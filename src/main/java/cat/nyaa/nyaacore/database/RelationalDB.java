package cat.nyaa.nyaacore.database;

import org.apache.commons.lang.NotImplementedException;

public interface RelationalDB extends Database {

    <T> Query<T> query(Class<T> tableClass);

    <T> TransactionalQuery<T> transaction(Class<T> tableClass);

    <T> TransactionalQuery<T> transaction(Class<T> tableClass, boolean manualCommit);

    void createTable(Class<?> cls);

    void updateTable(Class<?> cls);

    void deleteTable(Class<?> cls);

    void beginTransaction();

    void rollbackTransaction();

    void commitTransaction();

    default <T> Query<T> auto(Class<T> tableClass) {
        return new AutoQuery<>(transaction(tableClass));
    }

    default Class<?>[] getTables(){
        throw new NotImplementedException();
    }
}
