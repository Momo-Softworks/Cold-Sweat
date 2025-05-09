package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
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
    private final List<ConfigData.Type> registryTypes;

    public RemoveRegistryData(RegistryKey<Registry<T>> registry, NegatableList<CompoundNBT> matches, List<ResourceLocation> entries, List<Type> registryTypes)
    {
        super(new NegatableList<>());
        this.registry = registry;
        this.matches = matches;
        this.entries = entries;
        this.registryTypes = registryTypes;
    }

    private static final Codec<List<ConfigData.Type>> CONFIG_TYPE_CODEC = Codec.either(Type.CODEC, Type.CODEC.listOf())
                                                                          .xmap(either -> either.map(Arrays::asList, r -> r), Either::right);

    public static final Codec<RemoveRegistryData<? extends ConfigData>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(ModRegistries::getRegistry, ModRegistries::getRegistryName).fieldOf("registry").forGetter(data -> (RegistryKey) data.registry),
            NegatableList.codec(CompoundNBT.CODEC).optionalFieldOf("matches", new NegatableList<>()).forGetter(RemoveRegistryData::matches),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", Arrays.asList()).forGetter(RemoveRegistryData::entries),
            CONFIG_TYPE_CODEC.optionalFieldOf("config_type", Arrays.asList()).forGetter(RemoveRegistryData::configTypes)
    ).apply(instance, (key, matches, entries, type) -> new RemoveRegistryData<>((RegistryKey) key, (NegatableList) matches, (List) entries, type)));

    public RegistryKey<Registry<T>> registry()
    {   return registry;
    }
    public NegatableList<CompoundNBT> matches()
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
        if (this.entries().stream().anyMatch(id -> object.registryId().map(rl -> rl.equals(id)).orElse(false)))
        {   return true;
        }
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
