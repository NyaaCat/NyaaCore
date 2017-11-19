package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.sql.*;
import java.util.*;

public abstract class BaseDatabase implements Cloneable {

    /* TableName to TableStructure */
    protected final Map<String, TableStructure<?>> tables;
    /* TableClass to TableName */
    protected final Map<Class<?>, String> tableName;

    /**
     * The return value should be constant
     * and returned classes should be annotated by @DataTable().
     * @return Table classes in this database.
     */
    protected abstract Class<?>[] getTables();

    /* auto commit should be set to `true` for the returned connection */
    protected abstract Connection getConnection();

    public abstract void close();

    /**
     * Scan & construct all table structures.
     */
    protected BaseDatabase() {
        tables = new HashMap<>();
        tableName = new HashMap<>();
        for (Class<?> tableClass : getTables()) {
            TableStructure<?> tableStructure = TableStructure.fromClass(tableClass);
            if (tableStructure == null) throw new RuntimeException();
            tables.put(tableStructure.getTableName(), tableStructure);
            tableName.put(tableClass, tableStructure.getTableName());
        }
    }

    protected void createTables() {
        for (TableStructure<?> c : tables.values()) {
            createTable(c);
        }
    }

    protected void createTable(String name) {
        Validate.notNull(name);
        createTable(tables.get(name));
    }

    protected void createTable(Class<?> cls) {
        Validate.notNull(cls);
        createTable(tableName.get(cls));
    }

    /**
     * Create a table in this database.
     * Note the table doesn't have to be defined in getTables().
     * @param struct The table definition
     */
    protected void createTable(TableStructure<?> struct) {
        Validate.notNull(struct);
        String sql = struct.getCreateTableSQL();
        try {
            Statement smt = getConnection().createStatement();
            smt.executeUpdate(sql);
            smt.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * build a statement using provided parameters
     *
     * @param sql SQL string
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param parameters JDBC's positional parametrized query.
     * @return statement
     */
    public PreparedStatement buildStatement(String sql, Map<String, String> replacementMap, Object... parameters) {
        if (replacementMap != null) {
            for (String key : replacementMap.keySet()) {
                sql = sql.replace("{{" + key + "}}", replacementMap.get(key));
            }
        }
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            return stmt;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Convert a result set to a list of java objects
     * @param rs the result set
     * @param cls record type
     * @param <T> record type, do not have to be registered in getTables()
     * @return java object list
     */
    public <T> List<T> parseResultSet(ResultSet rs, Class<T> cls) {
        try {
            if (rs == null) return new ArrayList<>();
            TableStructure<T> table = TableStructure.fromClass(cls);
            List<T> results = new ArrayList<T>();
            while (rs.next()) {
                T obj = table.getObjectFromResultSet(rs);
                results.add(obj);
            }
            return results;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Return the Query object for specified table class.
     *
     * @return Query object
     */
    public <T> Query<T> query(Class<T> tableClass) {
        return new Query<>(tableClass);
    }

    public class Query<T> {
        private TableStructure<T> table;
        /* NOTE: the values in the map must be SQL-type objects */
        private Map<String, Object> whereClause = new HashMap<>();

        public Query(Class<T> tableClass) {
            if (!tableName.containsKey(tableClass)) throw new IllegalArgumentException("Unknown Table");
            if (!tables.containsKey(tableName.get(tableClass))) throw new IllegalArgumentException("Unknown Table");
            table = (TableStructure<T>) tables.get(tableName.get(tableClass));
        }


        /**
         * clear the where clauses
         *
         * @return self
         */
        public Query<T> clear() {
            whereClause.clear();
            return this;
        }

        public Query<T> whereEq(String columnName, Object obj) {
            return where(columnName, "=", obj);
        }

        /**
         * comparator can be any SQL comparator.
         * e.g. =, >, <
         */
        public Query<T> where(String columnName, String comparator, Object obj) {
            if (!table.hasColumn(columnName)) throw new IllegalArgumentException("Unknown DataColumn Name");
            obj = table.getColumn(columnName).columnType.toDatabaseType(obj);
            whereClause.put(columnName + comparator + "?", obj);
            return this;
        }

        /**
         * remove records matching the where clauses
         */
        public void delete() {
            String sql = "DELETE FROM " + table.getTableName();
            List<Object> objects = new ArrayList<>();
            if (whereClause.size() > 0) {
                sql += " WHERE";
                for (Map.Entry e : whereClause.entrySet()) {
                    if (objects.size() > 0) sql += " AND";
                    sql += " " + e.getKey();
                    objects.add(e.getValue());
                }
            }
            try {
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int x = 1;
                for (Object obj : objects) {
                    stmt.setObject(x, obj);
                    //ColumnType.setPreparedStatement(stmt, x, obj);
                    x++;
                }
                stmt.execute();
                stmt.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * the where clauses are ignored
         *
         * @param object record to be inserted
         */
        public void insert(T object) {
            try {
                String sql = String.format("INSERT INTO %s(%s) VALUES(?", table.getTableName(), table.getColumnNamesString());
                for (int i = 1; i < table.columns.size(); i++) sql += ",?";
                sql += ")";
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                Map<String, Object> objMap = table.getColumnObjectMap(object);
                for (int i = 1; i <= table.orderedColumnName.size(); i++) {
                    String colName = table.orderedColumnName.get(i - 1);
                    if (!objMap.containsKey(colName)) {
                        stmt.setNull(i, Types.NULL);
                    } else {
                        stmt.setObject(i, objMap.get(colName));
                    }
                }
                stmt.execute();
                stmt.close();
            } catch (SQLException | ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }


        /**
         * SELECT * FROM this_table WHERE ...
         * @return all select rows
         */
        public List<T> select() {
            String sql = "SELECT " + table.getColumnNamesString() + " FROM " + table.tableName;
            List<Object> objects = new ArrayList<>();
            if (whereClause.size() > 0) {
                sql += " WHERE";
                for (Map.Entry e : whereClause.entrySet()) {
                    if (objects.size() > 0) sql += " AND";
                    sql += " " + e.getKey();
                    objects.add(e.getValue());
                }
            }
            try {
                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int x = 1;
                for (Object obj : objects) {
                    stmt.setObject(x, obj);
                    x++;
                }
                List<T> results = new ArrayList<T>();
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    T obj = table.getObjectFromResultSet(rs);
                    results.add(obj);
                }
                stmt.close();
                return results;
            } catch (SQLException | ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Select only one record.
         *
         * @return the record, or throw exception if not unique
         */
        public T selectUnique() {
            List<T> results = select();
            if (results.size() < 1) throw new RuntimeException("SQL Selection has no result");
            if (results.size() > 1) throw new RuntimeException("SQL Selection result is not unique");
            return results.get(0);
        }

        /**
         * Select only one record.
         *
         * @return the record, or null if not unique.
         */
        public T selectUniqueUnchecked() {
            List<T> results = select();
            return results.size() == 1 ? results.get(0) : null;
        }

        /**
         * A short hand for select().size();
         * Note the potential performance issue.
         * @return number of records to be selected.
         */
        public int count() {
            return select().size();
        }

        /**
         * Update record according to the where clauses
         *
         * @param obj     new values for columns
         * @param columns columns need to be updated, update all columns if empty
         */
        public void update(T obj, String... columns) {
            try {
                List<String> updatedColumns = new ArrayList<>();
                Map<String, Object> newValues = table.getColumnObjectMap(obj, columns);
                if (columns == null || columns.length <= 0) {
                    updatedColumns.addAll(table.orderedColumnName);
                } else {
                    for (String col : columns) {
                        if (!table.columns.containsKey(col))
                            throw new IllegalArgumentException("Unknown Column Name: " + col);
                    }
                    updatedColumns.addAll(Arrays.asList(columns));
                }

                List<Object> parameters = new ArrayList<>();
                String sql = "UPDATE " + table.tableName + " SET ";
                for (int i = 0; i < updatedColumns.size(); i++) {
                    if (i > 0) sql += ",";
                    sql += updatedColumns.get(i) + "=?";
                    parameters.add(newValues.get(updatedColumns.get(i)));
                }

                boolean firstClause = true;
                if (whereClause.size() > 0) {
                    sql += " WHERE";
                    for (Map.Entry e : whereClause.entrySet()) {
                        if (!firstClause) sql += " AND";
                        firstClause = false;
                        sql += " " + e.getKey();
                        parameters.add(e.getValue());
                    }
                }

                PreparedStatement stmt = getConnection().prepareStatement(sql);
                int idx = 1;
                for (Object o : parameters) {
                    if (o == null) {
                        stmt.setNull(idx, Types.NULL);
                    } else {
                        stmt.setObject(idx, o);
                    }
                    idx++;
                }
                stmt.execute();
                stmt.close();
            } catch (ReflectiveOperationException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
