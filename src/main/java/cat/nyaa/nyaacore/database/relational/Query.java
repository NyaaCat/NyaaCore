package cat.nyaa.nyaacore.database.relational;

import java.util.List;

public interface Query<T> extends AutoCloseable {
    Query<T> reset();

    Query<T> whereEq(String columnName, Object obj);

    Query<T> where(String columnName, String comparator, Object obj);

    void delete();

    void insert(T object);

    List<T> select();

    T selectUnique();

    T selectUniqueUnchecked();

    int count();

    void update(T obj, String... columns);

    void rollback();

    void commit();
}
