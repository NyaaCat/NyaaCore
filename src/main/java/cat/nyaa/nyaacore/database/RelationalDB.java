package cat.nyaa.nyaacore.database;

public interface RelationalDB extends Database {

    <T> Query<T> query(Class<T> tableClass);

    void createTable(Class<?> cls);

    void updateTable(Class<?> cls);

    void deleteTable(Class<?> cls);

    void enableAutoCommit();

    void disableAutoCommit();
}
