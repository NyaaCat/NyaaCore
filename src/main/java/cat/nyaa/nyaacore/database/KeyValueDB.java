package cat.nyaa.nyaacore.database;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public interface KeyValueDB<K, V> extends Database{

    V get(K key);

    default CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key));
    }

    V get(K key, Function<? super K,? extends V> mappingFunction);

    default CompletableFuture<V> getAsync(K key, Function<? super K,? extends V> mappingFunction) {
        return CompletableFuture.supplyAsync(() -> get(key, mappingFunction));
    }

    void put(K key, V value);

    default CompletableFuture<Void> putAsync(K key, V value) {
        return CompletableFuture.supplyAsync(() -> {put(key, value);return null;});
    }

    Collection<V> getAll(K key);

    default CompletableFuture<Collection<V>> getAllAsync(K key) {
        return CompletableFuture.supplyAsync(() -> getAll(key));
    }

    Map<K, V> asMap();

    void clear();

    default CompletableFuture<Void> clearAsync() {
        return CompletableFuture.supplyAsync(() -> {clear();return null;});
    }
}
