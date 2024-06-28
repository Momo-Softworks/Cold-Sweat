package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.List;
import java.util.Optional;

public record DimensionTempData(List<Either<TagKey<DimensionType>, DimensionType>> dimensions, double temperature,
                                Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
{
    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryHelper.createTagCodec(Registries.DIMENSION_TYPE).listOf().fieldOf("dimensions").forGetter(DimensionTempData::dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(DimensionTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(DimensionTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(DimensionTempData::isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(DimensionTempData::requiredMods)
    ).apply(instance, DimensionTempData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DimensionTempData{dimensions=[");
        for (Either<TagKey<DimensionType>, DimensionType> dimension : dimensions)
        {
            if (dimension.left().isPresent())
            {   builder.append("#").append(dimension.left().get().toString());
            }
            else
            {   builder.append(dimension.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", units=").append(units).append(", isOffset=").append(isOffset);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
