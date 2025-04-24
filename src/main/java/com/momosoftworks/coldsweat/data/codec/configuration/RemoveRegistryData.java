package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.ModRegistries;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RemoveRegistryData<T extends ConfigData> extends ConfigData
{
    private final RegistryKey<Registry<T>> registry;
    private final NegatableList<CompoundNBT> matches;
    private final List<ResourceLocation> entries;

    public RemoveRegistryData(RegistryKey<Registry<T>> registry, NegatableList<CompoundNBT> matches, List<ResourceLocation> entries)
    {
        super(new NegatableList<>());
        this.registry = registry;
        this.matches = matches;
        this.entries = entries;
    }

    public static final Codec<RemoveRegistryData<? extends ConfigData>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(ModRegistries::getRegistry, ModRegistries::getRegistryName).fieldOf("registry").forGetter(data -> (RegistryKey) data.registry),
            NegatableList.codec(CompoundNBT.CODEC).fieldOf("matches").forGetter(data -> data.matches),
            ResourceLocation.CODEC.listOf().fieldOf("entries").forGetter(data -> data.entries)
    ).apply(instance, (key, matches, entries) -> new RemoveRegistryData<>((RegistryKey) key, (NegatableList) matches, (List) entries)));

    public RegistryKey<Registry<T>> registry()
    {   return registry;
    }
    public NegatableList<CompoundNBT> matches()
    {   return matches;
    }
    public List<ResourceLocation> entries()
    {   return entries;
    }

    public boolean matches(T object)
    {
        Optional<INBT> serializedOpt = ModRegistries.getCodec((RegistryKey) registry).encodeStart(NBTDynamicOps.INSTANCE, object).result();
        return serializedOpt.map(serialized ->
        {   return matches.test(nbt -> NbtRequirement.compareNbt(nbt, serialized, true));
        }).orElse(false);
    }

    @Override
    public Codec<? extends ConfigData> getCodec()
    {   return CODEC;
    }
}
