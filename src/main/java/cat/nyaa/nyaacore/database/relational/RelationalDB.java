package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.NotImplementedException;

import java.sql.Connection;

public interface RelationalDB extends Cloneable, AutoCloseable{
    /**
     * get the "default" connection
     */
    Connection getConnection();
    Connection newConnection();
    void recycleConnection(Connection conn);

    <T> SynchronizedQuery<T> query(Class<T> tableClass);
    <T> SynchronizedQuery<T> queryTransactional(Class<T> tableClass);

    void createTable(Class<?> cls);

    default void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    default void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }
}
