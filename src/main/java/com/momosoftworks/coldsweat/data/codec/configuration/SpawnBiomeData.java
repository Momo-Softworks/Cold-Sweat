package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class SpawnBiomeData implements IForgeRegistryEntry<SpawnBiomeData>
{
    public final List<Biome> biomes;
    public final EntityClassification category;
    public final int weight;
    public final List<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    public final Optional<List<String>> requiredMods;

    public SpawnBiomeData(List<Biome> biomes, EntityClassification category,
                          int weight, List<Either<ITag<EntityType<?>>, EntityType<?>>> entities, Optional<List<String>> requiredMods)
    {
        this.biomes = biomes;
        this.category = category;
        this.weight = weight;
        this.entities = entities;
        this.requiredMods = requiredMods;
    }

    public static final Codec<SpawnBiomeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.DIRECT_CODEC.listOf().fieldOf("biomes").forGetter(data -> data.biomes),
            EntityClassification.CODEC.fieldOf("category").forGetter(data -> data.category),
            Codec.INT.fieldOf("weight").forGetter(data -> data.weight),
            Codec.either(ITag.codec(EntityTypeTags::getAllTags), Registry.ENTITY_TYPE).listOf().fieldOf("entities").forGetter(data -> data.entities),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, SpawnBiomeData::new));

    @Override
    public SpawnBiomeData setRegistryName(ResourceLocation resourceLocation)
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
    public Class<SpawnBiomeData> getRegistryType()
    {
        return null;
    }
}