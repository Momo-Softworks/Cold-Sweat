package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public record StructureTempData(List<Either<TagKey<ConfiguredStructureFeature<?, ?>>, ResourceLocation>> structures, double temperature,
                                Temperature.Units units, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<StructureTempData>
{
    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation tagLocation = ResourceLocation.tryParse(string.replace("#", ""));
                if (tagLocation == null) throw new IllegalArgumentException("Structure tag is null");
                if (!string.contains("#")) return Either.<TagKey<ConfiguredStructureFeature<?, ?>>, ResourceLocation>right(tagLocation);

                return Either.<TagKey<ConfiguredStructureFeature<?, ?>>, ResourceLocation>left(TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, tagLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Structure tag is null");
                String result = tag.left().isPresent()
                                ? "#" + tag.left().get().location()
                                : tag.right().map(ResourceLocation::toString).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Structure field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(StructureTempData::requiredMods)
    ).apply(instance, StructureTempData::new));

    @Override
    public StructureTempData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<StructureTempData> getRegistryType()
    {
        return StructureTempData.class;
    }
}
