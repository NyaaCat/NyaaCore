package cat.nyaa.nyaacore.database;

public interface RelationalDB extends Database {

    <T> Query<T> query(Class<T> tableClass);

    <T> TransactionalQuery<T> transaction(Class<T> tableClass);

    void createTable(Class<?> cls);

    void updateTable(Class<?> cls);

    void deleteTable(Class<?> cls);

    void beginTransaction();

    void rollbackTransaction();

    void commitTransaction();

    default <T> Query<T> auto(Class<T> tableClass) {
        return new AutoQuery<>(transaction(tableClass));
    }
}
