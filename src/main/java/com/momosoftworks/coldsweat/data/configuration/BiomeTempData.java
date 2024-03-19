package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Optional;

public record BiomeTempData(List<Either<TagKey<Biome>, ResourceLocation>> biomes, double min, double max,
                            Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
{
    public static final Codec<BiomeTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation tagLocation = ResourceLocation.tryParse(string.replace("#", ""));
                if (tagLocation == null) throw new IllegalArgumentException("Biome tag is null");
                if (!string.contains("#")) return Either.<TagKey<Biome>, ResourceLocation>right(tagLocation);

                return Either.<TagKey<Biome>, ResourceLocation>left(TagKey.create(Registries.BIOME, tagLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Biome tag is null");
                String result = tag.left().isPresent()
                                ? "#" + tag.left().get().location()
                                : tag.right().map(ResourceLocation::toString).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Biome field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("biomes").forGetter(BiomeTempData::biomes),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"), Codec.DOUBLE.fieldOf("min_temp")).xmap(
            either ->
            {
                if (either.left().isPresent()) return either.left().get();
                if (either.right().isPresent()) return either.right().get();
                throw new IllegalArgumentException("Biome temperature min is not defined!");
            },
            Either::right).forGetter(BiomeTempData::min),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"), Codec.DOUBLE.fieldOf("max_temp")).xmap(
            either ->
            {
                if (either.left().isPresent()) return either.left().get();
                if (either.right().isPresent()) return either.right().get();
                throw new IllegalArgumentException("Biome temperature min is not defined!");
            },
            Either::right).forGetter(BiomeTempData::max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(BiomeTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(BiomeTempData::isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(BiomeTempData::requiredMods)
    ).apply(instance, BiomeTempData::new));
}
