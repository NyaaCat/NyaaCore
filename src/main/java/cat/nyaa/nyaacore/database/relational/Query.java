package cat.nyaa.nyaacore.database.relational;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * Common interface of relational database query
 *
 * Any instance members are not guaranteed to be thread safe. You should ensure that CompletableFuture have completed before calling another method.
 */

public interface Query<T> extends AutoCloseable {

    Query<T> reset();

    Query<T> whereEq(String columnName, Object obj);

    Query<T> where(String columnName, String comparator, Object obj);

    void delete();

    default CompletableFuture<Void> deleteAsync() {
        return CompletableFuture.runAsync(this::delete);
    }

    void insert(T object);

    default CompletableFuture<Void> insertAsync(T object) {
        return CompletableFuture.runAsync(() -> insert(object));
    }

    List<T> select();

    default CompletableFuture<List<T>> selectAsync() {
        return CompletableFuture.supplyAsync(this::select);
    }

    T selectUnique();

    default CompletableFuture<T> selectUniqueAsync() {
        return CompletableFuture.supplyAsync(this::selectUnique);
    }

    T selectUniqueUnchecked();

    default CompletableFuture<T> selectUniqueUncheckedAsync() {
        return CompletableFuture.supplyAsync(this::selectUniqueUnchecked);
    }

    int count();

    default CompletableFuture<Integer> countAsync() {
        return CompletableFuture.supplyAsync(this::count);
    }

    void update(T obj, String... columns);

    default CompletableFuture<Void> updateAsync(T obj, String... columns) {
        return CompletableFuture.runAsync(() -> update(obj, columns));
    }

    void rollback();

    void commit();
}
