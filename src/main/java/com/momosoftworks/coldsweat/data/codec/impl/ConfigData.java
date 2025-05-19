package com.momosoftworks.coldsweat.data.codec.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ConfigData implements NbtSerializable
{
    protected UUID id = UUID.randomUUID();
    protected Type registryType = Type.JSON;
    protected NegatableList<String> requiredMods;
    protected ResourceLocation registryId;

    protected static final Codec<Dummy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(Codec.STRING).optionalFieldOf("required_mods", new NegatableList<>()).forGetter(ConfigData::requiredMods),
            Type.CODEC.optionalFieldOf("config_type", Type.JSON).forGetter(ConfigData::registryType),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("id", UUID.randomUUID()).forGetter(ConfigData::uuid)
    ).apply(instance, Dummy::new));

    public ConfigData(NegatableList<String> requiredMods, Type configType, UUID id)
    {   this.requiredMods = requiredMods;
        this.registryType = configType;
        this.id = id;
    }

    public ConfigData(NegatableList<String> requiredMods)
    {   this.requiredMods = requiredMods;
    }

    public abstract Codec<? extends ConfigData> getCodec();

    protected static <T extends ConfigData> Codec<T> createCodec(Codec<T> child)
    {
        return new Codec<T>()
        {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input)
            {
                return child.decode(ops, input).map(pair ->
                {
                    T data = pair.getFirst();
                    CODEC.decode(ops, input).result().map(Pair::getFirst).ifPresent(dummy ->
                    {
                        data.requiredMods = dummy.requiredMods();
                        data.id = dummy.uuid();
                        data.registryType = dummy.registryType();
                    });
                    return Pair.of(data, input);
                });
            }

            @Override
            public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix)
            {
                Dummy dummy = new Dummy(input.requiredMods(), input.registryType(), input.uuid());
                return child.encode(input, ops, CODEC.encodeStart(ops, dummy).result().orElse(ops.empty()));
            }
        };
    }

    public UUID uuid()
    {
        if (this.id == null)
        {   this.id = UUID.randomUUID();
        }
        return id;
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

    public enum Type implements StringRepresentable
    {
        TOML("toml"),
        JSON("json"),
        KUBEJS("kubejs");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

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

    protected static class Dummy extends ConfigData
    {
        public Dummy(NegatableList<String> requiredMods, Type configType, UUID id)
        {   super(requiredMods, configType, id);
        }

        @Override
        public Codec<? extends ConfigData> getCodec()
        {   return CODEC;
        }
    }
}
