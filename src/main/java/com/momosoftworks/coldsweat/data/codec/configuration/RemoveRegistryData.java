package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Optional;

public class RemoveRegistryData<T extends ConfigData> extends ConfigData
{
    ResourceKey<Registry<T>> registry;
    NegatableList<CompoundTag> entries;

    public RemoveRegistryData(ResourceKey<Registry<T>> registry, NegatableList<CompoundTag> entries)
    {
        super(List.of());
        this.registry = registry;
        this.entries = entries;
    }

    public static final Codec<RemoveRegistryData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(s -> (ResourceKey)ModRegistries.getRegistry(s), key -> ModRegistries.getRegistryName(key)).fieldOf("registry").forGetter(data -> data.registry()),
            NegatableList.codec(CompoundTag.CODEC).fieldOf("matches").forGetter(data -> data.entries)
    ).apply(instance, RemoveRegistryData::new));

    public ResourceKey<Registry<T>> registry()
    {   return registry;
    }
    public NegatableList<CompoundTag> entries()
    {   return entries;
    }

    public boolean matches(T object)
    {
        Optional<Tag> serializedOpt = ModRegistries.getCodec((ResourceKey) registry).encodeStart(NbtOps.INSTANCE, object).result();
        return serializedOpt.map(serialized ->
        {   return entries.test(nbt -> NbtRequirement.compareNbt(nbt, serialized, true));
        }).orElse(false);
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }
}
