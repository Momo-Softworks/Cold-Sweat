package com.momosoftworks.coldsweat.data.codec.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.momosoftworks.coldsweat.api.annotation.Internal;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class ConfigData
{
    protected UUID id = UUID.randomUUID();
    protected Type configType = Type.JSON;
    protected NegatableList<String> requiredMods;
    protected Holder holder;

    public static final Codec<NegatableList<String>> REQUIRED_MODS_CODEC = NegatableList.listCodec(Codec.STRING, true);
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    public static final Codec<Type> TYPE_CODEC = Type.CODEC;

    public ConfigData(NegatableList<String> requiredMods, Type configType, UUID id)
    {   this.requiredMods = requiredMods;
        this.configType = configType;
        this.id = id;
    }

    public ConfigData(NegatableList<String> requiredMods)
    {   this.requiredMods = requiredMods;
    }

    public abstract <T> Codec<T> getCodec();

    protected static <T extends ConfigData> Codec<T> createCodec(MapCodec<T> child)
    {
        return new MapCodec<T>()
        {
            @Override
            public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix)
            {
                RecordBuilder<O> builder = child.encode(input, ops, prefix);
                builder.add("required_mods", input.requiredMods(), REQUIRED_MODS_CODEC)
                       .add("config_type", input.configType(), TYPE_CODEC)
                       .add("id", input.uuid(), UUID_CODEC);
                input.holder().flatMap(Holder::unwrapKey).ifPresent(key ->
                {
                    builder.add("registry_name", key.registry(), ResourceLocation.CODEC);
                    builder.add("registry_key", key.location(), ResourceLocation.CODEC);
                });
                return builder;
            }

            @Override
            public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input)
            {
                return child.decode(ops, input).flatMap(instance ->
                {
                    instance.requiredMods = decodeFromMap("required_mods", ops, input, REQUIRED_MODS_CODEC, new NegatableList<>());
                    instance.configType = decodeFromMap("config_type", ops, input, TYPE_CODEC, Type.JSON);
                    instance.id = decodeFromMap("id", ops, input, UUID_CODEC, null);
                    ResourceLocation registry = decodeFromMap("registry_name", ops, input, ResourceLocation.CODEC, null);
                    ResourceLocation key = decodeFromMap("registry_key", ops, input, ResourceLocation.CODEC, null);
                    // Set decoded object's holder reference if available
                    if (registry != null && key != null && ops instanceof RegistryOps<O> registryOps)
                    {
                        registryOps.<T>getter(ResourceKey.createRegistryKey(registry)).ifPresent(registryGetter ->
                        {
                            ResourceKey<T> registryKey = ResourceKey.create(ResourceKey.createRegistryKey(registry), key);
                            registryGetter.get(registryKey).ifPresent(instance::setHolder);
                        });

                    }
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

    public Type configType()
    {   return configType;
    }

    public NegatableList<String> requiredMods()
    {   return requiredMods;
    }

    public <T> Optional<Holder<T>> holder()
    {   return Optional.ofNullable((Holder<T>) holder);
    }

    @Internal
    public void setId(UUID id)
    {   this.id = id;
    }

    @Internal
    public void setConfigType(Type configType)
    {   this.configType = configType;
    }

    @Internal
    public <T> void setHolder(Holder<T> holder)
    {   this.holder = holder;
    }

    @Override
    public String toString()
    {   return this.getClass().getSimpleName() + this.getCodec().encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("");
    }

    public boolean areRequiredModsLoaded()
    {   return requiredMods.test(mod -> mod.equals("minecraft") || CompatManager.modLoaded(mod));
    }

    public <T> boolean is(TagKey<T> tag)
    {
        try
        {   return this.<T>holder().map(h -> h.is(tag)).orElse(false);
        }
        catch (ClassCastException e)
        {   return false;
        }
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

        public static final Codec<Type> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String name;

        Type(String name)
        {   this.name = name;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }
    }
}
