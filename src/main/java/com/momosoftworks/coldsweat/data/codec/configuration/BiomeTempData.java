package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record BiomeTempData(List<Either<TagKey<Biome>, Biome>> biomes, double min, double max,
                            Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods) implements IForgeRegistryEntry<BiomeTempData>
{
    public static final Codec<BiomeTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrVanillaRegistryCodec(Registry.BIOME_REGISTRY, Biome.CODEC).listOf().fieldOf("biomes").forGetter(BiomeTempData::biomes),
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

    @Override
    public BiomeTempData setRegistryName(ResourceLocation name)
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
    public Class<BiomeTempData> getRegistryType()
    {
        return null;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BiomeTempData{biomes=[");
        for (Either<TagKey<Biome>, Biome> biome : biomes)
        {
            if (biome.left().isPresent())
            {   builder.append("#").append(biome.left().get().toString());
            }
            else
            {   builder.append(biome.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], min=").append(min).append(", max=").append(max).append(", units=").append(units).append(", isOffset=").append(isOffset);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");

        return builder.toString();
    }
}
