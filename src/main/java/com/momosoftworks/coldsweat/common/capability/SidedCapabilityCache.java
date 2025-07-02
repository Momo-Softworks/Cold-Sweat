package com.momosoftworks.coldsweat.common.capability;


import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SidedCapabilityCache<C, K extends IAttachmentHolder> extends CapabilityCache<C, K>
{
    protected final CapabilityCache<C, K> clientCache;

    public SidedCapabilityCache(Supplier<AttachmentType<C>> capability, Predicate<K> invalidator)
    {
        super(capability, invalidator);
        this.clientCache = new CapabilityCache<>(capability, invalidator);
    }

    public SidedCapabilityCache(Supplier<AttachmentType<C>> capability)
    {
        super(capability);
        this.clientCache = new CapabilityCache<>(capability);
    }

    @Override
    public C get(K key)
    {   return EffectiveSide.get().isClient() ? clientCache.get(key) : super.get(key);
    }

    @Override
    public void remove(K key)
    {
        if (EffectiveSide.get().isClient())
        {   clientCache.remove(key);
        }
        else super.remove(key);
    }

    public void clearClient()
    {   clientCache.clear();
    }

    public void clearServer()
    {   super.clear();
    }

    @Override
    public void clear()
    {
        if (EffectiveSide.get().isClient())
        {   this.clearClient();
        }
        else this.clearServer();
    }

    @Override
    public void ifPresent(K key, Consumer<C> consumer)
    {
        if (EffectiveSide.get().isClient())
        {   clientCache.ifPresent(key, consumer);
        }
        else  super.ifPresent(key, consumer);
    }
}
