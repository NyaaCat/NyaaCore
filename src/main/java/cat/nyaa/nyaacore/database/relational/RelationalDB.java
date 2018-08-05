package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.NotImplementedException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Every relational database has a default connection
 * which will be connected and closed automatically
 */
public interface RelationalDB extends Cloneable, AutoCloseable {
    /**
     * get the "default" connection
     */
    Connection getConnection();

    /**
     * Create a new connection, or get one from the pool
     * depending the implementation
     *
     * @return the new connection
     */
    Connection newConnection();

    /**
     * Close the connection or put it back into pool.
     *
     * @param conn the connection
     */
    void recycleConnection(Connection conn);

    /**
     * Create a query that works directly on the default connection.
     * New table may be created using the default connection.
     *
     * @param tableClass the table class type
     * @param <T>        the table to be worked on
     * @return the query
     */
    <T> SynchronizedQuery.NonTransactionalQuery<T> query(Class<T> tableClass);

    /**
     * @param tableClass
     * @param <T>
     * @return
     */
    <T> SynchronizedQuery.TransactionalQuery<T> queryTransactional(Class<T> tableClass);

    /**
     * forcefully create a table in the database
     */
    void createTable(Class<?> cls) throws SQLException;

    /**
     * @deprecated Unfinished API, subjects to changes
     */
    @Deprecated
    default void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    /**
     * @deprecated Unfinished API, subjects to changes
     */
    @Deprecated
    default void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    @Override
    void close();

    /**
     * Begin a transation on default connection
     */
    default void beginTransation() {
        throw new NotImplementedException();
    }

    /**
     * Rollback current transation on default connection
     */
    default void rollbackTransation() {
        throw new NotImplementedException();
    }

    /**
     * Commit current transation on default connection
     */
    default void commitTransation() {
        throw new NotImplementedException();
    }
}
