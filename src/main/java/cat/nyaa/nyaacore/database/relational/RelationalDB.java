package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.NotImplementedException;

public interface RelationalDB extends Cloneable, AutoCloseable {

    @SuppressWarnings("unchecked")
    <T> T connect();

    <T> SynchronizedQuery<T> query(Class<T> tableClass);

    <T> SynchronizedQuery<T> queryTransactional(Class<T> tableClass);

    void createTable(Class<?> cls);

    default void updateTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    default void deleteTable(Class<?> cls) {
        throw new NotImplementedException();
    }

    void beginTransaction();

    void rollbackTransaction();

    void commitTransaction();

    default Class<?>[] getTables() {
        throw new NotImplementedException();
    }

    <T> SynchronizedQuery<T> queryTransactional(Class<T> tableClass, boolean commitOnClose);
}
