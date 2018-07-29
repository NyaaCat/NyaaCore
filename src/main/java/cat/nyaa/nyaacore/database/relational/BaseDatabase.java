package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.Validate;

import java.sql.*;
import java.util.*;

public abstract class BaseDatabase implements RelationalDB {
    protected Set<Class> createdTableClasses = new HashSet<>();

    @Override
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL();
        try {
            Statement smt = getConnection().createStatement();
            smt.executeUpdate(sql);
            smt.close();
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * build a statement using provided parameters
     *
     * @param sql            SQL string
     * @param replacementMap {{key}} in the file will be replaced by value. Ignored if null. NOTE: sql injection will happen
     * @param parameters     JDBC's positional parametrized query.
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
            throw new RuntimeException(sql, ex);
        }
    }

    /**
     * Convert a result set to a list of java objects
     *
     * @param rs  the result set
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
     * Return the SynchronizedQuery object for specified table class.
     *
     * @return SynchronizedQuery object
     */
    @Override
    public <T> SynchronizedQuery.NonTransactionalQuery<T> query(Class<T> tableClass) {
        createTable(tableClass);
        return new SynchronizedQuery.NonTransactionalQuery<T>(tableClass, this.getConnection()) {
            @Override
            public void close() throws Exception {

            }
        };
    }

    @Override
    public <T> SynchronizedQuery.TransactionalQuery<T> queryTransactional(Class<T> tableClass) {
        createTable(tableClass);
        Connection conn = newConnection();
        try {
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return new SynchronizedQuery.TransactionalQuery<T>(tableClass, conn) {
            @Override
            public void close() throws Exception {
                super.close();
                recycleConnection(conn);
            }
        };
    }
}
