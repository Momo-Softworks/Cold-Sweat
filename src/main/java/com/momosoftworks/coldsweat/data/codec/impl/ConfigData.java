package com.momosoftworks.coldsweat.data.codec.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ConfigData implements NbtSerializable
{
    private UUID id = UUID.randomUUID();
    private Type registryType;
    NegatableList<String> requiredMods;

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

    @Internal
    public void setId(UUID id)
    {   this.id = id;
    }

    @Internal
    public void setRegistryType(Type registryType)
    {   this.registryType = registryType;
    }

    @Override
    public CompoundTag serialize()
    {   return (CompoundTag) ((Codec<ConfigData>) this.getCodec()).encodeStart(NbtOps.INSTANCE, this).result().orElse(new CompoundTag());
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
        return obj instanceof ConfigData data
                && data.requiredMods().equals(this.requiredMods());
    }

    public enum Type implements StringRepresentable
    {
        TOML("toml"),
        JSON("json"),
        KUBEJS("kubejs");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values, Type::byName);

        private final String name;

        Type(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        @Nullable
        public static Type byName(String name)
        {
            for (Type type : Type.values())
            {
                if (type.getSerializedName().equals(name))
                    return type;
            }
            return null;
        }
    }
}
