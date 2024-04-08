package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class DimensionTempData implements IForgeRegistryEntry<DimensionTempData>
{
    List<ResourceLocation> dimensions;
    double temperature;
    Temperature.Units units;
    boolean isOffset;
    Optional<List<String>> requiredMods;

    public DimensionTempData(List<ResourceLocation> dimensions, double temperature,
                             Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
    {
        this.dimensions = dimensions;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
        this.requiredMods = requiredMods;
    }

    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("dimension").forGetter(data -> data.dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            com.momosoftworks.coldsweat.api.util.Temperature.Units.CODEC.optionalFieldOf("units", com.momosoftworks.coldsweat.api.util.Temperature.Units.MC).forGetter(data -> data.units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(data -> data.isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, DimensionTempData::new));

    @Override
    public DimensionTempData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<DimensionTempData> getRegistryType()
    {
        return DimensionTempData.class;
    }
}
