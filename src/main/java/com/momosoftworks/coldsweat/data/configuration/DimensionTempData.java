package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record DimensionTempData(List<Either<TagKey<DimensionType>, DimensionType>> dimensions, double temperature,
                                Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
{
    public static final Codec<DimensionTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.createVanillaTagCodec(Registry.DIMENSION_TYPE_REGISTRY).listOf().fieldOf("dimensions").forGetter(DimensionTempData::dimensions),
            Codec.DOUBLE.fieldOf("temperature").forGetter(DimensionTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(DimensionTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(DimensionTempData::isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(DimensionTempData::requiredMods)
    ).apply(instance, DimensionTempData::new));
}
