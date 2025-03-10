package com.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Predicate;

public class SidedCapabilityCache<C, K extends ICapabilityProvider> extends CapabilityCache<C, K>
{
    protected final CapabilityCache<C, K> clientCache;

    public SidedCapabilityCache(Supplier<Capability<C>> capability, Predicate<K> invalidator)
    {
        super(capability, invalidator);
        this.clientCache = new CapabilityCache<>(capability, invalidator);
    }

    public SidedCapabilityCache(Supplier<Capability<C>> capability)
    {
        super(capability);
        this.clientCache = new CapabilityCache<>(capability);
    }

    @Override
    public LazyOptional<C> get(K key)
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

    @Override
    public void ifLazyPresent(K key, Consumer<LazyOptional<C>> consumer)
    {
        if (EffectiveSide.get().isClient())
        {   clientCache.ifLazyPresent(key, consumer);
        }
        else super.ifLazyPresent(key, consumer);
    }
}
