package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.keyvalue.KeyValueDB;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MapDatabase<K, V> implements KeyValueDB<K, V> {
    private Map<K, V> map = new HashMap<>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(K key) {
        return map.remove(key);
    }

    @Override
    public Collection<V> getAll(K key) {
        return Collections.singleton(map.get(key));
    }

    @Override
    public Map<K, V> asMap() {
        return map;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(K key){
        return map.containsKey(key);
    }

    @Override
    public void close() throws Exception {
        map = null;
    }
}
