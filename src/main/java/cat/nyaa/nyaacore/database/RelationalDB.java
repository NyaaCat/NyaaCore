package cat.nyaa.nyaacore.database;

public interface RelationalDB extends Database {

    <T> Query<T> query(Class<T> tableClass);

    <T> TransactionalQuery<T> transaction(Class<T> tableClass);

    void createTable(Class<?> cls);

    void updateTable(Class<?> cls);

    void deleteTable(Class<?> cls);

    /**
     * deprecated see {@link RelationalDB#transaction(Class)}
     */
    @Deprecated
    void enableAutoCommit();

    /**
     * @deprecated see {@link RelationalDB#transaction(Class)}
     */
    @Deprecated
    void disableAutoCommit();

    default <T> AutoQuery<T> auto(Class<T> tableClass) {
        return new AutoQuery<>(transaction(tableClass));
    }
}
