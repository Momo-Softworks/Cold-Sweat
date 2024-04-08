package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.Map;

public class ResourceKey<T>
{
    private static final Map<String, RegistryKey<?>> VALUES = Collections.synchronizedMap(Maps.newIdentityHashMap());

    public static <T> Codec<RegistryKey<T>> codec(RegistryKey<? extends Registry<T>> p_195967_) {
        return ResourceLocation.CODEC.xmap((p_195979_) -> {
            return create(p_195967_, p_195979_);
        }, RegistryKey::location);
    }

    public static <T> RegistryKey<T> create(RegistryKey<? extends Registry<T>> pRegistryKey, ResourceLocation pLocation) {
        String s = (pRegistryKey + ":" + pLocation).intern();
        return (RegistryKey<T>)VALUES.computeIfAbsent(s, (p_195971_) -> {
            return RegistryKey.create(pRegistryKey, pLocation);
        });
    }
}
