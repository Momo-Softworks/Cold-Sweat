package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Optional;

public record SpawnBiomeData(List<Either<TagKey<Biome>, Biome>> biomes, MobCategory category,
                             int weight, List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities, Optional<List<String>> requiredMods)
{
    public static final Codec<SpawnBiomeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryHelper.createTagCodec(Registries.BIOME).listOf().fieldOf("biomes").forGetter(SpawnBiomeData::biomes),
            MobCategory.CODEC.fieldOf("category").forGetter(SpawnBiomeData::category),
            Codec.INT.fieldOf("weight").forGetter(SpawnBiomeData::weight),
            Codec.either(TagKey.codec(Registries.ENTITY_TYPE), BuiltInRegistries.ENTITY_TYPE.byNameCodec()).listOf().fieldOf("entities").forGetter(SpawnBiomeData::entities),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(SpawnBiomeData::requiredMods)
    ).apply(instance, SpawnBiomeData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SpawnBiomeData{biomes=[");
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
        builder.append("], category=").append(category).append(", weight=").append(weight).append(", entities=[");
        for (Either<TagKey<EntityType<?>>, EntityType<?>> entity : entities)
        {
            if (entity.left().isPresent())
            {   builder.append("#").append(entity.left().get().toString());
            }
            else
            {   builder.append(entity.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("]");
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}