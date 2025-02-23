package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.LocationRequirement;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnBiomeData extends ConfigData
{
    final List<Biome> biomes;
    final EntityClassification category;
    final int weight;
    final List<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    final IntegerBounds count;
    final LocationRequirement location;
    final LocationRequirement blockBelow;

    public SpawnBiomeData(List<Biome> biomes, EntityClassification category,
                          int weight, List<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                          IntegerBounds count, LocationRequirement location,
                          LocationRequirement blockBelow, List<String> requiredMods)
    {
        super(requiredMods);
        this.biomes = biomes;
        this.category = category;
        this.weight = weight;
        this.entities = entities;
        this.count = count;
        this.location = location;
        this.blockBelow = blockBelow;
    }

    public SpawnBiomeData(List<Biome> biomes, EntityClassification category,
                          int weight, List<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                          IntegerBounds count, LocationRequirement location, LocationRequirement blockBelow)
    {
        this(biomes, category, weight, entities, count, location, blockBelow, ConfigHelper.getModIDs(biomes, Registry.BIOME_REGISTRY));
    }

    public SpawnBiomeData(Collection<Biome> biomes, EntityClassification category,
                          int weight, Collection<EntityType<?>> entities,
                          IntegerBounds count, LocationRequirement location, LocationRequirement blockBelow)
    {
        this(new ArrayList<>(biomes),
             category, weight,
             entities.stream().map(Either::<ITag<EntityType<?>>, EntityType<?>>right).collect(Collectors.toList()),
             count, location, blockBelow);
    }

    public static final Codec<SpawnBiomeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.dynamicCodec(Registry.BIOME_REGISTRY).listOf().fieldOf("biomes").forGetter(data -> data.biomes),
            EntityClassification.CODEC.fieldOf("category").forGetter(data -> data.category),
            Codec.INT.fieldOf("weight").forGetter(data -> data.weight),
            Codec.either(ITag.codec(EntityTypeTags::getAllTags), Registry.ENTITY_TYPE).listOf().fieldOf("entities").forGetter(data -> data.entities),
            IntegerBounds.CODEC.optionalFieldOf("count", IntegerBounds.NONE).forGetter(SpawnBiomeData::count),
            LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(SpawnBiomeData::location),
            LocationRequirement.CODEC.optionalFieldOf("block_below", LocationRequirement.NONE).forGetter(SpawnBiomeData::blockBelow),
            Codec.STRING.listOf().optionalFieldOf("required_mods", Arrays.asList()).forGetter(SpawnBiomeData::requiredMods)
    ).apply(instance, SpawnBiomeData::new));

    public List<Biome> biomes()
    {   return biomes;
    }
    public EntityClassification category()
    {   return category;
    }
    public int weight()
    {   return weight;
    }
    public List<Either<ITag<EntityType<?>>, EntityType<?>>> entities()
    {   return entities;
    }
    public IntegerBounds count()
    {   return count;
    }
    public LocationRequirement location()
    {   return location;
    }
    public LocationRequirement blockBelow()
    {   return blockBelow;
    }

    @Nullable
    public static SpawnBiomeData fromToml(List<?> entry, EntityType<?> entityType, DynamicRegistries registryAccess)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing entity spawn biome config: not enough arguments");
            return null;
        }
        List<Biome> biomes = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, registryAccess, (String) entry.get(0));
        if (biomes.isEmpty()) return null;
        return new SpawnBiomeData(biomes, EntityClassification.CREATURE, ((Number) entry.get(1)).intValue(),
                                  Arrays.asList(Either.right(entityType)), new IntegerBounds(1, 1), LocationRequirement.NONE, LocationRequirement.NONE);
    }

    @Override
    public Codec<SpawnBiomeData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SpawnBiomeData that = (SpawnBiomeData) obj;
        return biomes.equals(that.biomes)
            && category == that.category
            && weight == that.weight
            && entities.equals(that.entities)
            && count.equals(that.count)
            && location.equals(that.location)
            && blockBelow.equals(that.blockBelow);
    }
}