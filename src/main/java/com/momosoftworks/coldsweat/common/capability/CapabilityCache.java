package com.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CapabilityCache<C, K extends ICapabilityProvider>
{
    protected final Map<K, LazyOptional<C>> cache = new HashMap<>();
    protected final Predicate<K> invalidator;
    protected final Capability<C> capability;

    public CapabilityCache(Capability<C> capability, Predicate<K> invalidator)
    {   this.capability = capability;
        this.invalidator = invalidator;
    }

    public CapabilityCache(Capability<C> capability)
    {   this.capability = capability;
        this.invalidator = null;
    }

    public LazyOptional<C> get(K key)
    {
        LazyOptional<C> existing = cache.get(key);
        if (existing != null && existing.isPresent()) return existing;

        this.cleanExpiredEntries();
        LazyOptional<C> cap = key.getCapability(capability);
        cap.addListener((opt) -> cache.remove(key));
        cache.put(key, cap);

        return cap;
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

    public void ifPresent(K key, Consumer<C> consumer)
    {
        LazyOptional<C> cap = this.get(key);
        if (cap == null) return;
        cap.resolve().ifPresent(consumer);
    }

    public void ifLazyPresent(K key, Consumer<LazyOptional<C>> consumer)
    {
        LazyOptional<C> cap = cache.get(key);
        if (cap != null)
        {   consumer.accept(cap);
        }
    }

    public void removeIf(Predicate<K> predicate)
    {
        List<K> removedKeys = new ArrayList<>(cache.size());
        for (Map.Entry<K, LazyOptional<C>> entry : cache.entrySet())
        {
            K key = entry.getKey();
            LazyOptional<C> value = entry.getValue();
            if (predicate.test(key) || !value.isPresent())
            {   removedKeys.add(key);
            }
        }
        for (int i = 0; i < removedKeys.size(); i++)
        {   cache.remove(removedKeys.get(i));
        }
    }
}
