package com.momosoftworks.coldsweat.data;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.util.exceptions.RegistryFailureException;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class RegistryHolder<V extends ConfigData>
{
    private final RegistryKey<Registry<V>> key;
    private final Codec<V> codec;
    private final Class<V> type;
    private final Map<ResourceLocation, V> data = new HashMap<>();

    public RegistryHolder(RegistryKey<Registry<V>> key, Codec<V> codec, Class<V> type)
    {
        this.key = key;
        this.codec = codec;
        this.type = type;
    }

    public RegistryKey<Registry<V>> key()
    {   return key;
    }

    public Codec<V> codec()
    {   return codec;
    }

    public Class<V> type()
    {   return type;
    }

    public Map<ResourceLocation, V> data()
    {   return data;
    }

    public void register(ResourceLocation id, V data)
    {
        if (this.data.put(id, data) != null)
        {   throw ColdSweat.LOGGER.throwing(new RegistryFailureException(data, key.location().toString(), "Duplicate entry", null));
        }
    }

    public void flush()
    {   data.clear();
    }
}
