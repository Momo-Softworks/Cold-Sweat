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

public class DimensionTempData implements IForgeRegistryEntry<DimensionTempData>
{
    ResourceLocation dimension;
    double temperature;
    Temperature.Units units;
    boolean isOffset;

    public DimensionTempData(ResourceLocation dimension, double temperature, Temperature.Units units, boolean isOffset)
    {
        this.dimension = dimension;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
    }

    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(data -> data.dimension),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            com.momosoftworks.coldsweat.api.util.Temperature.Units.CODEC.optionalFieldOf("units", com.momosoftworks.coldsweat.api.util.Temperature.Units.MC).forGetter(data -> data.units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(data -> data.isOffset)
    ).apply(instance, DimensionTempData::new));

    @Override
    public DimensionTempData setRegistryName(ResourceLocation name)
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
    public Class<DimensionTempData> getRegistryType()
    {
        return DimensionTempData.class;
    }
}
