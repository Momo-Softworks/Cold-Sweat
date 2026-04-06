package com.momosoftworks.coldsweat.mixin_interface;

import net.minecraft.core.Registry;

public interface RegistryTagLoader<T>
{
    Registry<T> getRegistry();
    void setRegistry(Registry<T> registry);
}
