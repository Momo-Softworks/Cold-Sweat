package com.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Predicate;

public class CapabilityCache<C, K extends ICapabilityProvider>
{
    protected final Map<K, LazyOptional<C>> cache = new WeakHashMap<>();
    protected final Predicate<K> invalidator;
    protected final Supplier<Capability<C>> capability;

    public CapabilityCache(Supplier<Capability<C>> capability, Predicate<K> invalidator)
    {   this.capability = capability;
        this.invalidator = invalidator;
    }

    public CapabilityCache(Supplier<Capability<C>> capability)
    {   this.capability = capability;
        this.invalidator = null;
    }

    public LazyOptional<C> get(K key)
    {
        this.cleanExpiredEntries();
        return cache.computeIfAbsent(key, e ->
        {
            LazyOptional<C> cap = e.getCapability(capability.get());
            cap.addListener((opt) -> cache.remove(e));
            return cap;
        });
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
        synchronized (cache)
        {
            if (this.invalidator != null)
            {
                List<K> removedKeys = new ArrayList<>();
                for (Map.Entry<K, LazyOptional<C>> entry : cache.entrySet())
                {
                    K key = entry.getKey();
                    LazyOptional<C> value = entry.getValue();
                    if (invalidator.test(key) || !value.isPresent())
                    {   removedKeys.add(key);
                    }
                }
                for (int i = 0; i < removedKeys.size(); i++)
                {   cache.remove(removedKeys.get(i));
                }
            }
        }
    }

    public void ifPresent(K key, Consumer<C> consumer)
    {
        LazyOptional<C> cap = cache.get(key);
        if (cap != null && cap.resolve().isPresent())
        {   consumer.accept(cap.resolve().get());
        }
    }

    public void ifLazyPresent(K key, Consumer<LazyOptional<C>> consumer)
    {
        LazyOptional<C> cap = cache.get(key);
        if (cap != null)
        {   consumer.accept(cap);
        }
    }

    public void removeIf(Predicate<K> predicate)
    {   cache.entrySet().removeIf(e -> predicate.test(e.getKey()));
    }
}
