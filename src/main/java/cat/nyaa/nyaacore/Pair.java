package cat.nyaa.nyaacore;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Pair<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Pair(Map.Entry<? extends K, ? extends V> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public Pair<K, V> copy() {
        return new Pair<>(this.key, this.value);
    }

    public Pair<K, V> deepCopy(Function<K, K> keyMapper, Function<V, V> valueMapper) {
        K keyCopy = key == null ? null : keyMapper.apply(key);
        V valueCopy = value == null ? null : valueMapper.apply(value);
        return new Pair<>(keyCopy, valueCopy);
    }

    public static <Ks, Vs> Pair<Ks, Vs> of(Ks key, Vs value) {
        return new Pair<>(key, value);
    }

    @Override
    public K getKey() {
        return key;
    }

    public K setKey(K key) {
        K oldKey = this.key;
        this.key = key;
        return oldKey;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) * 17 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair pair) {
            return Objects.equals(key, pair.getKey()) && Objects.equals(value, pair.getValue());
        }
        return false;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}
