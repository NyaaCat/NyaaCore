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
        try (Statement smt = getConnection().createStatement()) {
            smt.executeUpdate(sql);
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    public abstract Connection getConnection();

    protected abstract Connection newConnection();

    protected abstract void recycleConnection(Connection conn);

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
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
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
    public <T> SynchronizedQuery<T> query(Class<T> tableClass) {
        createTable(tableClass);
        return new SynchronizedQuery<>(tableClass, this.getConnection());
    }

    @Override
    public <T> SynchronizedQuery<T> queryTransactional(Class<T> tableClass) {
        return queryTransactional(tableClass, true);
    }

    @Override
    public <T> SynchronizedQuery<T> queryTransactional(Class<T> tableClass, boolean commitOnClose) {
        createTable(tableClass);
        Connection conn = newConnection();
        try {
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return new SynchronizedQuery<T>(tableClass, conn, commitOnClose) {
            @Override
            public void close() {
                if (commitOnClose) {
                    commit();
                } else {
                    rollback();
                }
                recycleConnection(conn);
            }
        };
    }
}
