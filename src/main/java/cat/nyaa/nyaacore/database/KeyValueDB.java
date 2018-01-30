package cat.nyaa.nyaacore.database;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface KeyValueDB<K, V> extends Database{

    V get(K key);

    V get(K key, Function<? super K,? extends V> mappingFunction);

    void put(K key, V value);

    Collection<V> getAll(K key);

    Map<K,V> asMap();

    void clear();
}
