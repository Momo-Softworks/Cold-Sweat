package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record DimensionTempData(List<Either<TagKey<DimensionType>, DimensionType>> dimensions, double temperature,
                                Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<DimensionTempData>
{
    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryHelper.createVanillaTagCodec(Registry.DIMENSION_TYPE_REGISTRY).listOf().fieldOf("dimensions").forGetter(DimensionTempData::dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(DimensionTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(DimensionTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(DimensionTempData::isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(DimensionTempData::requiredMods)
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
        return null;
    }

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
