package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;

import java.util.List;
import java.util.Optional;

public class DimensionTempData
{
    public final List<DimensionType> dimensions;
    public final double temperature;
    public final Temperature.Units units;
    public final boolean isOffset;
    public final Optional<List<String>> requiredMods;

    public DimensionTempData(List<DimensionType> dimensions, double temperature,
                             Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
    {
        this.dimensions = dimensions;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
        this.requiredMods = requiredMods;
    }

    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.dynamicCodec(Registry.DIMENSION_TYPE_REGISTRY).listOf().fieldOf("dimension").forGetter(data -> data.dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(data -> data.units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(data -> data.isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, DimensionTempData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DimensionTempData{dimensions=[");
        for (DimensionType dimension : dimensions)
        {
            builder.append(dimension.toString());
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", units=").append(units).append(", isOffset=").append(isOffset);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
