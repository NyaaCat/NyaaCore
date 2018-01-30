package cat.nyaa.nyaacore.database;

public interface RelationalDB extends Database {

    public <T> Query<T> query(Class<T> tableClass);

    void createTable(Class<?> cls);

    void updateTable(Class<?> cls);

    void deleteTable(Class<?> cls);
}
