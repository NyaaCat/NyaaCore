package cat.nyaa.nyaacore.database.keyvalue;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface KeyValueDB<K, V> extends AutoCloseable {

    int size();

    V get(K key);

    V get(K key, Function<? super K, ? extends V> mappingFunction);

    Collection<V> getAll(K key);

    Map<K, V> asMap();

    V put(K key, V value);

    V remove(K key);

    void clear();

    @Override
    void close();

    default boolean containsKey(K key) {
        return get(key) != null;
    }
}
