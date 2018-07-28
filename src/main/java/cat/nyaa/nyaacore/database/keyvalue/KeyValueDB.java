package cat.nyaa.nyaacore.database.keyvalue;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public interface KeyValueDB<K, V> {

    int size();

    V get(K key);

    default CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key));
    }

    V get(K key, Function<? super K,? extends V> mappingFunction);

    default CompletableFuture<V> getAsync(K key, Function<? super K,? extends V> mappingFunction) {
        return CompletableFuture.supplyAsync(() -> get(key, mappingFunction));
    }

    Collection<V> getAll(K key);

    default CompletableFuture<Collection<V>> getAllAsync(K key) {
        return CompletableFuture.supplyAsync(() -> getAll(key));
    }

    Map<K, V> asMap();

    V put(K key, V value);

    default CompletableFuture<V> putAsync(K key, V value) {
        return CompletableFuture.supplyAsync(() -> put(key, value));
    }

    V remove(K key);

    default CompletableFuture<V> removeAsync(K key) {
        return CompletableFuture.supplyAsync(() -> remove(key));
    }

    void clear();

    default CompletableFuture<Void> clearAsync() {
        return CompletableFuture.supplyAsync(() -> {clear();return null;});
    }

    default boolean containsKey(K key){
        return get(key) != null;
    }
}
