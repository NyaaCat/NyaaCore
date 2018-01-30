package cat.nyaa.nyaacore.database;

import java.util.List;

public interface Query<T> {

    Query<T> clear();

    Query<T> whereEq(String columnName, Object obj);

    Query<T> where(String columnName, String comparator, Object obj);

    void delete();

    void insert(T object);

    List<T> select();

    T selectUnique();

    int count();

    void update(T obj, String... columns);
}
