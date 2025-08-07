package com.momosoftworks.coldsweat.data;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public record RegistryHolder<V extends ConfigData>(ResourceKey<Registry<V>> key, Codec<V> codec, Class<V> type)
{}
