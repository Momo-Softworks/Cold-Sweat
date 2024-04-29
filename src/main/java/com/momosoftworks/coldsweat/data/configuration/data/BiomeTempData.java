package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Optional;

public class BiomeTempData implements IForgeRegistryEntry<BiomeTempData>
{
    List<ResourceLocation> biomes;
    double min;
    double max;
    Temperature.Units units;
    boolean isOffset;
    Optional<List<String>> requiredMods;

    public BiomeTempData(List<ResourceLocation> biomes, double min, double max, Temperature.Units units, boolean isOffset, Optional<List<String>> requiredMods)
    {
        this.biomes = biomes;
        this.min = min;
        this.max = max;
        this.units = units;
        this.isOffset = isOffset;
        this.requiredMods = requiredMods;
    }

    public static final Codec<BiomeTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().fieldOf("biomes").forGetter(data -> data.biomes),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"), Codec.DOUBLE.fieldOf("min_temp")).xmap(
                    either ->
                    {
                        if (either.left().isPresent()) return either.left().get();
                        if (either.right().isPresent()) return either.right().get();
                        throw new IllegalArgumentException("Biome temperature min is not defined!");
                    },
                    Either::right).forGetter(data -> data.min),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"), Codec.DOUBLE.fieldOf("max_temp")).xmap(
                    either ->
                    {
                        if (either.left().isPresent()) return either.left().get();
                        if (either.right().isPresent()) return either.right().get();
                        throw new IllegalArgumentException("Biome temperature min is not defined!");
                    },
                    Either::right).forGetter(data -> data.max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(data -> data.units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(data -> data.isOffset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, BiomeTempData::new));

    @Override
    public BiomeTempData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<BiomeTempData> getRegistryType()
    {
        return BiomeTempData.class;
    }
}
