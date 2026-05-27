package com.momosoftworks.coldsweat.util.math;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MappedCache<K, V>
{
    protected final Map<K, V> cache = new HashMap<>();
    protected final Predicate<K> invalidator;
    protected final Function<K, V> getter;

    public MappedCache(Function<K, V> getter, Predicate<K> invalidator)
    {   this.getter = getter;
        this.invalidator = invalidator;
    }

    public MappedCache(Function<K, V> getter)
    {   this.getter = getter;
        this.invalidator = null;
    }

    public V get(K key)
    {
        V existing = cache.get(key);
        if (existing != null && !this.isInvalid(key))
        {   return existing;
        }
        this.cleanExpiredEntries();
        return this.getFresh(key);
    }

    /**
     * Forces the cache to retrieve a new value for the key and returns the result
     */
    public V getFresh(K key)
    {
        V value = getter.apply(key);
        cache.put(key, value);
        return value;
    }

    public void set(K key, V value)
    {   cache.put(key, value);
    }

    public int size()
    {   return cache.size();
    }

    public void remove(K key)
    {   cache.remove(key);
    }

    public void clear()
    {   cache.clear();
    }

    protected void cleanExpiredEntries()
    {
        if (this.invalidator != null)
        {   this.removeIf(this.invalidator);
        }
    }

    public void ifPresent(K key, Consumer<V> consumer)
    {
        V value = this.get(key);
        if (value == null) return;
        consumer.accept(value);
    }

    public void removeIf(Predicate<K> predicate)
    {   cache.entrySet().removeIf(entry -> predicate.test(entry.getKey()) || entry.getValue() == null);
    }

    protected boolean isInvalid(K key)
    {   return this.invalidator != null && this.invalidator.test(key);
    }
}
