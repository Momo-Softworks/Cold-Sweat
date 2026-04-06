package com.momosoftworks.coldsweat.mixin_interface;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;

public interface RegistryTagLoader<T>
{
    Registry<T> getRegistry();
    void setRegistry(Registry<T> registry);
    Map<ResourceLocation, Collection<Holder<T>>> getTags();
}
