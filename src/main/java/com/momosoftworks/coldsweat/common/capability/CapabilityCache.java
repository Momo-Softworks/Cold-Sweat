package com.momosoftworks.coldsweat.common.capability;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CapabilityCache<C, K extends IAttachmentHolder>
{
    protected final Map<K, C> cache = new HashMap<>();
    protected final Predicate<K> invalidator;
    protected final Supplier<AttachmentType<C>> capability;

    public CapabilityCache(Supplier<AttachmentType<C>> capability, Predicate<K> invalidator)
    {   this.capability = capability;
        this.invalidator = invalidator;
    }

    public CapabilityCache(Supplier<AttachmentType<C>> capability)
    {   this.capability = capability;
        this.invalidator = null;
    }

    public C get(K key)
    {
        C existing = cache.get(key);
        if (existing != null) return existing;

        this.cleanExpiredEntries();
        C cap = key.getData(capability);
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
        C cap = this.get(key);
        if (cap != null)
        {   consumer.accept(cap);
        }
    }

    public void removeIf(Predicate<K> predicate)
    {
        List<K> removedKeys = new ArrayList<>(cache.size());
        for (Map.Entry<K, C> entry : cache.entrySet())
            {
                K key = entry.getKey();
                if (predicate.test(key))
            {   removedKeys.add(key);
            }
        }
        for (int i = 0; i < removedKeys.size(); i++)
        {   cache.remove(removedKeys.get(i));
        }
    }
}
