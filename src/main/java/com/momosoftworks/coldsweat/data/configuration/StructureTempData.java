package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;

import java.util.List;
import java.util.Optional;

public record StructureTempData(List<Either<TagKey<ConfiguredStructureFeature<?,?>>, ResourceLocation>> structures, double temperature,
                                Temperature.Units units, Optional<List<String>> requiredMods)
{
    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(TagKey.codec(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY), ResourceLocation.CODEC).listOf().fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(StructureTempData::requiredMods)
    ).apply(instance, StructureTempData::new));
}
