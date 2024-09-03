package com.momosoftworks.coldsweat.util.math;

import com.google.common.collect.BiMap;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public class FastBiMap<K, V> implements BiMap<K, V>, Serializable {
    private final FastMap<K, V> forward;
    private final FastMap<V, K> backward;
    private transient InverseMap inverse;

    public FastBiMap() {
        this(256);
    }

    public FastBiMap(int capacity) {
        forward = new FastMap<>(capacity);
        backward = new FastMap<>(capacity);
    }

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backward.containsKey(value);
    }

    @Override
    public V get(Object key) {
        return forward.getEqual(key);
    }

    public K getKey(V value) {
        return backward.getEqual(value);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = forward.put(key, value);
        if (oldValue != null) {
            backward.remove(oldValue);
        }
        K oldKey = backward.put(value, key);
        if (oldKey != null && !oldKey.equals(key)) {
            forward.remove(oldKey);
        }
        return oldValue;
    }

    @Override
    public @Nullable V forcePut(K key, V value)
    {
        V oldValue = forward.put(key, value);
        backward.put(value, key);
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        V value = forward.remove(key);
        if (value != null) {
            backward.remove(value);
        }
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();
    }

    @Override
    public Set<K> keySet() {
        return forward.keySet();
    }

    @Override
    public Set<V> values() {
        return backward.keySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new InvertibleEntrySet();
    }

    public FastBiMap<V, K> inverse() {
        InverseMap result = inverse;
        return (result == null) ? inverse = new InverseMap() : result;
    }

    private class InverseMap extends FastBiMap<V, K> {
        InverseMap() {
            super(FastBiMap.this.size());
        }

        @Override
        public K get(Object key) {
            return FastBiMap.this.getKey((V) key);
        }

        @Override
        public K put(V key, K value) {
            return FastBiMap.this.getKey(FastBiMap.this.put(value, key));
        }

        @Override
        public Set<Entry<V, K>> entrySet() {
            return new InvertibleEntrySet() {
                @Override
                public Iterator<Entry<V, K>> iterator() {
                    return new InvertibleEntryIterator(FastBiMap.this.backward);
                }
            };
        }

        @Override
        public FastBiMap<K, V> inverse() {
            return FastBiMap.this;
        }
    }

    private class InvertibleEntrySet extends AbstractSet<Entry<K, V>> {
        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new InvertibleEntryIterator(forward);
        }

        @Override
        public int size() {
            return FastBiMap.this.size();
        }
    }

    private class InvertibleEntryIterator implements Iterator<Entry<K, V>> {
        private final Iterator<Entry<K, V>> delegate;

        InvertibleEntryIterator(FastMap<K, V> map) {
            this.delegate = map.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return new InvertibleEntry(delegate.next());
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }

    private class InvertibleEntry implements Entry<K, V> {
        private final Entry<K, V> delegate;

        InvertibleEntry(Entry<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public K getKey() {
            return delegate.getKey();
        }

        @Override
        public V getValue() {
            return delegate.getValue();
        }

        @Override
        public V setValue(V value) {
            V oldValue = delegate.setValue(value);
            backward.remove(oldValue);
            backward.put(value, getKey());
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return getKey().equals(e.getKey()) &&
                    getValue().equals(e.getValue());
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString()
        {
            return getKey() + "=" + getValue();
        }
    }
}
