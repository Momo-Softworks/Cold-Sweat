package com.momosoftworks.coldsweat.common.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SidedCapabilityCache<C, K extends ICapabilityProvider> extends CapabilityCache<C, K>
{
    protected final Predicate<K> invalidator;
    protected final CapabilityCache<C, K> clientCache;

    public SidedCapabilityCache(Capability<C> capability, Predicate<K> invalidator)
    {
        super(capability, invalidator);
        this.invalidator = invalidator;
        this.clientCache = new CapabilityCache<>(capability, invalidator);
    }

    @Override
    public LazyOptional<C> get(K key)
    {
        boolean isClient = EffectiveSide.get().isClient();
        return isClient ? clientCache.get(key) : super.get(key);
    }

    @Override
    public void remove(K key)
    {
        boolean isClient = EffectiveSide.get().isClient();
        if (isClient)
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
        else
        {   this.clearServer();
        }
    }

    @Override
    public void ifPresent(K key, Consumer<C> consumer)
    {
        boolean isClient = EffectiveSide.get().isClient();
        if (isClient)
        {   clientCache.ifPresent(key, consumer);
        }
        else
        {   super.ifPresent(key, consumer);
        }
    }

    @Override
    public void ifLazyPresent(K key, Consumer<LazyOptional<C>> consumer)
    {
        boolean isClient = EffectiveSide.get().isClient();
        if (isClient)
        {   clientCache.ifLazyPresent(key, consumer);
        }
        else
        {   super.ifLazyPresent(key, consumer);
        }
    }
}
