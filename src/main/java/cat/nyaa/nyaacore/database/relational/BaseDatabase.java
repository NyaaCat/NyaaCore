package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.Validate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
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

    @Override
    public void beginTransation() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollbackTransation() {
        try {
            getConnection().rollback();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commitTransation() {
        try {
            getConnection().commit();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            public void close() {

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
            public void close() {
                super.close();
                recycleConnection(conn);
            }
        };
    }
}
