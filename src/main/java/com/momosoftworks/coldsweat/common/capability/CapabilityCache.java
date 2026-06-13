package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.util.math.MappedCache;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CapabilityCache<C, K extends ICapabilityProvider> extends MappedCache<K, LazyOptional<C>>
{
    protected final Capability<C> capability;

    public CapabilityCache(Capability<C> capability, Predicate<K> invalidator)
    {
        super(e -> e.getCapability(capability), invalidator);
        this.capability = capability;
    }

    public CapabilityCache(Capability<C> capability)
    {
        super(e -> e.getCapability(capability));
        this.capability = capability;
    }

    @Override
    public LazyOptional<C> get(K key)
    {
        LazyOptional<C> existing = super.get(key);
        if (!existing.isPresent()) return this.getFresh(key);
        return existing;
    }

    public void ifLazyPresent(K key, Consumer<LazyOptional<C>> consumer)
    {
        LazyOptional<C> cap = cache.get(key);
        if (cap != null)
        {   consumer.accept(cap);
        }
    }
}
