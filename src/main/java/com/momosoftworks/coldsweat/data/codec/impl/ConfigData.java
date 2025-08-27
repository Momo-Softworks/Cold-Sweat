package com.momosoftworks.coldsweat.data.codec.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class ConfigData implements NbtSerializable
{
    protected UUID id = UUID.randomUUID();
    protected Type registryType = Type.JSON;
    protected NegatableList<String> requiredMods;

    protected static final Codec<NegatableList<String>> REQUIRED_MODS_CODEC = NegatableList.listCodec(Codec.STRING);
    protected static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    protected static final Codec<Type> TYPE_CODEC = Type.CODEC;

    public ConfigData(NegatableList<String> requiredMods, Type configType, UUID id)
    {   this.requiredMods = requiredMods;
        this.registryType = configType;
        this.id = id;
    }

    public ConfigData(NegatableList<String> requiredMods)
    {   this.requiredMods = requiredMods;
    }

    public abstract Codec<? extends ConfigData> getCodec();

    protected static <T extends ConfigData> Codec<T> createCodec(MapCodec<T> child)
    {
        return new MapCodec<T>()
        {
            @Override
            public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix)
            {
                return child.encode(input, ops, prefix)
                        .add("required_mods", input.requiredMods(), REQUIRED_MODS_CODEC)
                        .add("config_type", input.registryType(), TYPE_CODEC)
                        .add("id", input.uuid(), UUID_CODEC);
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input)
            {
                return child.decode(ops, input).flatMap(instance ->
                {
                    instance.requiredMods = decodeFromMap("required_mods", ops, input, REQUIRED_MODS_CODEC, new NegatableList<>());
                    instance.registryType = decodeFromMap("config_type", ops, input, TYPE_CODEC, Type.JSON);
                    instance.id = decodeFromMap("id", ops, input, UUID_CODEC, null);
                    return DataResult.success(instance);
                });
            }

            @Override
            public <O> Stream<O> keys(DynamicOps<O> ops)
            {   return Stream.of("required_mods", "config_type", "id").map(ops::createString);
            }
        }.codec();
    }

    private static <T, O> T decodeFromMap(String key, DynamicOps<O> ops, MapLike<O> input, Codec<T> codec, T defaultValue)
    {
        return codec.decode(ops, input.get(key)).result().map(Pair::getFirst).orElse(defaultValue);
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
