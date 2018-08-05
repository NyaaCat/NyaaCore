package cat.nyaa.nyaacore.database.relational;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * A simple interface to build the SQL string.
 * Use {@link #getConnection()} if you need anything complex.
 * @param <T> the table class
 */
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

    void commit();

    void rollback();

    Connection getConnection();

    @Override
    void close();
}
