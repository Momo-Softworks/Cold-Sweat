package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record SpawnBiomeData(List<Either<TagKey<Biome>, Biome>> biomes, MobCategory category,
                             int weight, List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities, Optional<List<String>> requiredMods)
{
    public static final Codec<SpawnBiomeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.createForgeTagCodec(ForgeRegistries.BIOMES, Registry.BIOME_REGISTRY).listOf().fieldOf("biomes").forGetter(SpawnBiomeData::biomes),
            MobCategory.CODEC.fieldOf("category").forGetter(SpawnBiomeData::category),
            Codec.INT.fieldOf("weight").forGetter(SpawnBiomeData::weight),
            Codec.either(TagKey.codec(Registry.ENTITY_TYPE_REGISTRY), ForgeRegistries.ENTITY_TYPES.getCodec()).listOf().fieldOf("entities").forGetter(SpawnBiomeData::entities),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(SpawnBiomeData::requiredMods)
    ).apply(instance, SpawnBiomeData::new));
}