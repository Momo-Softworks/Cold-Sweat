package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.LocationRequirement;
import com.momosoftworks.coldsweat.data.codec.util.BlockInWorld;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SpawnBiomeBuilderJS
{
    public final Set<Biome> biomes = new HashSet<>();
    public EntityClassification category;
    public int weight;
    public final Set<EntityType<?>> entities = new HashSet<>();
    public IntegerBounds count;
    public NegatableList<LocationRequirement> locationPredicate = new NegatableList<>();

    public SpawnBiomeBuilderJS()
    {}

    public SpawnBiomeBuilderJS biomes(String... biomes)
    {
        DynamicRegistries registryAccess = RegistryHelper.getDynamicRegistries();
        if (registryAccess == null)
        {   ColdSweat.LOGGER.error("Could not parse biomes for addSpawnBiomes(), as registries are not built yet.");
            return this;
        }
        NegatableList<Biome> biomesList = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, registryAccess, biomes);
        this.biomes.addAll(biomesList.flatList());
        return this;
    }

    public SpawnBiomeBuilderJS category(String category)
    {
        for (EntityClassification mobCategory : EntityClassification.values())
        {
            if (mobCategory.getName().equals(category))
            {
                this.category = mobCategory;
                return this;
            }
        }
        return this;
    }

    public SpawnBiomeBuilderJS weight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public SpawnBiomeBuilderJS entities(String... entities)
    {
        this.entities.addAll(RegistryHelper.mapTaggableList(ConfigHelper.getEntityTypes(entities)));
        return this;
    }

    public SpawnBiomeBuilderJS count(int min, int max)
    {
        this.count = new IntegerBounds(min, max);
        return this;
    }

    public SpawnBiomeBuilderJS locationPredicate(Predicate<BlockInWorld> locationPredicate)
    {
        this.locationPredicate.add(new LocationRequirement(locationPredicate), false);
        return this;
    }

    public SpawnBiomeData build()
    {
        SpawnBiomeData data = new SpawnBiomeData(biomes, category, weight, entities, count, locationPredicate);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
