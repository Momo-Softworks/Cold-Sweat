package com.momosoftworks.coldsweat.data.codec.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public abstract class ConfigData implements NbtSerializable
{
    private UUID id = UUID.randomUUID();
    private Type registryType;
    private NegatableList<String> requiredMods;
    private ResourceLocation registryId;

    public ConfigData(NegatableList<String> requiredMods)
    {   this.requiredMods = requiredMods;
    }

    public abstract Codec<? extends ConfigData> getCodec();

    public UUID uuid()
    {   return id;
    }

    public Type registryType()
    {   return registryType;
    }

    public NegatableList<String> requiredMods()
    {   return requiredMods;
    }

    public Optional<ResourceLocation> registryId()
    {   return Optional.of(registryId);
    }

    @Internal
    public void setId(UUID id)
    {   this.id = id;
    }

    @Internal
    public void setRegistryType(Type registryType)
    {   this.registryType = registryType;
    }

    @Internal
    public void setRegistryId(ResourceLocation registryId)
    {   this.registryId = registryId;
    }

    @Override
    public CompoundNBT serialize()
    {   return (CompoundNBT) ((Codec<ConfigData>) this.getCodec()).encodeStart(NBTDynamicOps.INSTANCE, this).result().orElse(new CompoundNBT());
    }

    @Override
    public String toString()
    {   return this.getClass().getSimpleName() + ((Codec) getCodec()).encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("");
    }

    public boolean areRequiredModsLoaded()
    {   return requiredMods.test(mod -> mod.equals("minecraft") || CompatManager.modLoaded(mod));
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof ConfigData
                && ((ConfigData) obj).requiredMods().equals(this.requiredMods());
    }

    public enum Type
    {
        TOML,
        JSON,
        KUBEJS
    }
}
