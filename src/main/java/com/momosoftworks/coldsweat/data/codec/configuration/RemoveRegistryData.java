package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RemoveRegistryData<T extends ConfigData> extends ConfigData implements IForgeRegistryEntry<RemoveRegistryData<?>>
{
    private final ResourceKey<Registry<T>> registry;
    private final NegatableList<CompoundTag> matches;
    private final List<ResourceLocation> entries;
    private final List<ConfigData.Type> registryTypes;

    public RemoveRegistryData(ResourceKey<Registry<T>> registry, NegatableList<CompoundTag> matches, List<ResourceLocation> entries, List<Type> registryTypes)
    {
        super(new NegatableList<>());
        this.registry = registry;
        this.matches = matches;
        this.entries = entries;
        this.registryTypes = registryTypes;
    }

    private static final Codec<List<ConfigData.Type>> CONFIG_TYPE_CODEC = Codec.either(Type.CODEC, Type.CODEC.listOf())
                                                                          .xmap(either -> either.map(List::of, r -> r), Either::right);

    public static final Codec<RemoveRegistryData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(s -> (ResourceKey)ModRegistries.getRegistry(s), key -> ModRegistries.getRegistryName(key)).fieldOf("registry").forGetter(data -> data.registry()),
            NegatableList.listCodec(CompoundTag.CODEC).optionalFieldOf("matches", new NegatableList<>()).forGetter(RemoveRegistryData::matches),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(RemoveRegistryData::entries),
            CONFIG_TYPE_CODEC.optionalFieldOf("config_type", List.of()).forGetter(RemoveRegistryData::configTypes)
    ).apply(instance, RemoveRegistryData::new));

    public ResourceKey<Registry<T>> registry()
    {   return registry;
    }
    public NegatableList<CompoundTag> matches()
    {   return matches;
    }
    public List<ResourceLocation> entries()
    {   return entries;
    }
    public List<ConfigData.Type> configTypes()
    {   return registryTypes;
    }

    private boolean checkType(T object)
    {
        return this.registryTypes.isEmpty()
            || this.registryTypes.contains(object.registryType());
    }

    public boolean matches(T object)
    {
        if (!checkType(object))
        {   return false;
        }
        Optional<Tag> serializedOpt = ModRegistries.getCodec((ResourceKey) registry).encodeStart(NbtOps.INSTANCE, object).result();
        return serializedOpt.map(serialized ->
        {   return matches.test(nbt -> NbtRequirement.compareNbt(nbt, serialized, true));
        }).orElse(false);
    }

    public boolean matches(Holder<T> holder)
    {
        if (!checkType(holder.value()))
        {   return false;
        }
        // Check if object ID is in the entries list
        ResourceLocation key = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (key != null && entries.contains(key))
        {   return true;
        }
        return this.matches(holder.value());
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }

    @Override
    public RemoveRegistryData<?> setRegistryName(ResourceLocation resourceLocation)
    {
        return null;
    }

    @Override
    public @Nullable ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<RemoveRegistryData<?>> getRegistryType()
    {
        return null;
    }
}
