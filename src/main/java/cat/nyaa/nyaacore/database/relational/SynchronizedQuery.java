package cat.nyaa.nyaacore.database.relational;

import javax.persistence.NonUniqueResultException;
import java.sql.*;
import java.util.*;

/**
 * Synchronously working on the given connection
 *
 * @param <T> the table type
 */
public abstract class SynchronizedQuery<T> implements Query<T> {
    protected TableStructure<T> table;
    protected Connection conn;

    /* NOTE: the values in the map must be SQL-type objects */
    protected Map<String, Object> whereClause = new HashMap<>();

    public SynchronizedQuery(Class<T> tableClass, Connection conn) {
        this.conn = conn;
        this.table = TableStructure.fromClass(tableClass);
    }

    /**
     * reset the where clauses
     *
     * @return self
     */
    @Override
    public SynchronizedQuery<T> reset() {
        whereClause.clear();
        return this;
    }

    @Override
    public SynchronizedQuery<T> whereEq(String columnName, Object obj) {
        return where(columnName, "=", obj);
    }

    /**
     * comparator can be any SQL comparator.
     * e.g. =, >, <
     */
    @SuppressWarnings("unchecked")
    @Override
    public SynchronizedQuery<T> where(String columnName, String comparator, Object obj) {
        if (!table.hasColumn(columnName)) throw new IllegalArgumentException("Unknown DataColumn Name");
        obj = table.getColumn(columnName).typeConverter.toSqlType(obj);
        whereClause.put(columnName + comparator + "?", obj);
        return this;
    }

    /**
     * remove records matching the where clauses
     */
    @Override
    public void delete() {
        String sql = "DELETE FROM " + table.getTableName();
        List<Object> objects = new ArrayList<>();
        sql = buildWhereClause(sql, objects);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * the where clauses are ignored
     *
     * @param object record to be inserted
     */
    @Override
    public void insert(T object) {
        String sql = String.format("INSERT INTO %s(%s) VALUES(?", table.getTableName(), table.getColumnNamesString());
        for (int i = 1; i < table.columns.size(); i++) sql += ",?";
        sql += ")";
        Map<String, Object> objMap = table.getColumnObjectMap(object);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= table.orderedColumnName.size(); i++) {
                String colName = table.orderedColumnName.get(i - 1);
                if (!objMap.containsKey(colName) || objMap.get(colName) == null) {
                    stmt.setNull(i, Types.NULL);
                } else {
                    stmt.setObject(i, objMap.get(colName));
                }
            }
            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException(sql + "\n" + objMap.toString(), ex);
        }
    }

    /**
     * SELECT * FROM this_table WHERE ...
     *
     * @return all select rows
     */
    @Override
    public List<T> select() {
        String sql = "SELECT " + table.getColumnNamesString() + " FROM " + table.tableName;
        List<Object> objects = new ArrayList<>();
        sql = buildWhereClause(sql, objects);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            List<T> results = new ArrayList<T>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T obj = table.getObjectFromResultSet(rs);
                    results.add(obj);
                }
            }
            return results;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    private String buildWhereClause(String sql, List<Object> objects) {
        if (whereClause.size() > 0) {
            sql += " WHERE";
            for (Map.Entry<?, ?> e : whereClause.entrySet()) {
                if (objects.size() > 0) sql += " AND";
                sql += " " + e.getKey();
                objects.add(e.getValue());
            }
        }
        return sql;
    }

    /**
     * Select only one record.
     *
     * @return the record, or throw exception if not unique
     */
    @Override
    public T selectUnique() {
        T result = selectUniqueUnchecked();
        if (result == null) {
            throw new NonUniqueResultException("SQL Selection has no result or not unique");
        }
        return result;
    }

    /**
     * Select only one record.
     *
     * @return the record, or null if not unique.
     */
    @Override
    public T selectUniqueUnchecked() {
        String sql = "SELECT " + table.getColumnNamesString() + " FROM " + table.tableName;
        List<Object> objects = new ArrayList<>();
        sql = buildWhereClause(sql, objects);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            T result = null;
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result = table.getObjectFromResultSet(rs);
                    if (rs.next()) result = null;
                }
            }
            return result;
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * A short hand for select().size();
     *
     * @return number of records to be selected.
     */
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) AS C FROM " + table.tableName;
        List<Object> objects = new ArrayList<>();
        sql = buildWhereClause(sql, objects);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int x = 1;
            for (Object obj : objects) {
                stmt.setObject(x, obj);
                x++;
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("C");
                    return count;
                } else {
                    throw new RuntimeException("COUNT() returns empty result");
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * Update record according to the where clauses
     *
     * @param obj     new values for columns
     * @param columns columns need to be updated, update all columns if empty
     */
    @Override
    public void update(T obj, String... columns) {
        List<String> updatedColumns = new ArrayList<>();
        Map<String, Object> newValues = table.getColumnObjectMap(obj, columns);
        if (columns == null || columns.length <= 0) {
            updatedColumns.addAll(table.orderedColumnName);
        } else {
            for (String col : columns) {
                if (!table.columns.containsKey(col)) {
                    throw new IllegalArgumentException("Unknown Column Name: " + col);
                }
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
            for (Map.Entry<?, ?> e : whereClause.entrySet()) {
                if (!firstClause) sql += " AND";
                firstClause = false;
                sql += " " + e.getKey();
                parameters.add(e.getValue());
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    public abstract static class NonTransactionalQuery<T> extends SynchronizedQuery<T> {
        public NonTransactionalQuery(Class<T> tableClass, Connection conn) {
            super(tableClass, conn);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void commit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rollback() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * All transactions will automatically rollback on close.
     * If no commit or rollback executed.
     */
    public abstract static class TransactionalQuery<T> extends SynchronizedQuery<T> {
        protected boolean rollbackOnClose = true;

        public TransactionalQuery(Class<T> tableClass, Connection conn) {
            super(tableClass, conn);
            try {
                conn.setAutoCommit(false);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void commit() {
            rollbackOnClose = false;
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void rollback() {
            rollbackOnClose = false;
            try {
                conn.rollback();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            if (rollbackOnClose) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
