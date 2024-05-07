package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class StructureTempData implements IForgeRegistryEntry<StructureTempData>
{
    public List<ResourceLocation> structures;
    public double temperature;
    public Temperature.Units units;
    public Optional<List<String>> requiredMods;
    
    public StructureTempData(List<ResourceLocation> structures, double temperature,
                             Temperature.Units units, Optional<List<String>> requiredMods)
    {   
        this.structures = structures;
        this.temperature = temperature;
        this.units = units;
        this.requiredMods = requiredMods;
    }
    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("structures").forGetter(data -> data.structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(data -> data.units),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, StructureTempData::new));

    @Override
    public StructureTempData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<StructureTempData> getRegistryType()
    {
        return null;
    }
}
