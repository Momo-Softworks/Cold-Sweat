package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.LocationRequirement;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SpawnBiomeData extends ConfigData implements IForgeRegistryEntry<SpawnBiomeData>
{
    final NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes;
    final NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities;
    final MobCategory category;
    final int weight;
    final IntegerBounds count;
    final NegatableList<LocationRequirement> location;

    public SpawnBiomeData(NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes,
                          NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities,
                          MobCategory category, int weight, IntegerBounds count, NegatableList<LocationRequirement> location,
                          NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.biomes = biomes;
        this.entities = entities;
        this.category = category;
        this.weight = weight;
        this.count = count;
        this.location = location;
    }

    public SpawnBiomeData(NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes, NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities,
                          MobCategory category, int weight, IntegerBounds count, NegatableList<LocationRequirement> location)
    {
        this(biomes, entities, category, weight, count, location, new NegatableList<>());
    }

    public SpawnBiomeData(Collection<OptionalHolder<Biome>> biomes, MobCategory category,
                          int weight, Collection<EntityType<?>> entities,
                          IntegerBounds count, NegatableList<LocationRequirement> location)
    {
        this(new NegatableList<>(biomes.stream().map(Either::<TagKey<Biome>, OptionalHolder<Biome>>right).toList()),
             new NegatableList<>(entities.stream().map(Either::<TagKey<EntityType<?>>, EntityType<?>>right).toList()),
             category, weight, count, location);
    }

    public static final Codec<SpawnBiomeData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.BIOME_REGISTRY)).fieldOf("biomes").forGetter(SpawnBiomeData::biomes),
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, ForgeRegistries.ENTITIES)).fieldOf("entities").forGetter(SpawnBiomeData::entities),
            MobCategory.CODEC.fieldOf("category").forGetter(SpawnBiomeData::category),
            Codec.INT.fieldOf("weight").forGetter(SpawnBiomeData::weight),
            IntegerBounds.CODEC.optionalFieldOf("count", IntegerBounds.NONE).forGetter(SpawnBiomeData::count),
            NegatableList.codec(LocationRequirement.CODEC).optionalFieldOf("location", new NegatableList<>()).forGetter(SpawnBiomeData::location)
    ).apply(instance, SpawnBiomeData::new)));

    public NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes()
    {   return biomes;
    }
    public MobCategory category()
    {   return category;
    }
    public int weight()
    {   return weight;
    }
    public NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities()
    {   return entities;
    }
    public IntegerBounds count()
    {   return count;
    }
    public NegatableList<LocationRequirement> location()
    {   return location;
    }

    @Nullable
    public static SpawnBiomeData fromToml(List<?> entry, EntityType<?> entityType, RegistryAccess registryAccess)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing entity spawn biome config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, registryAccess, (String) entry.get(0));
        if (biomes.isEmpty()) return null;
        return new SpawnBiomeData(biomes, new NegatableList<>(Either.right(entityType)),
                                  MobCategory.CREATURE, ((Number) entry.get(1)).intValue(),
                                  new IntegerBounds(1, 1), new NegatableList<>());
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
            && location.equals(that.location);
    }

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