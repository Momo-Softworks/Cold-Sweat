package com.momosoftworks.coldsweat.data.biome_modifier;

import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModBiomeModifiers;
import com.momosoftworks.coldsweat.data.codec.util.FunctionalSpawnerData;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.Collection;


public record AddSpawnsBiomeModifier(boolean useConfigs) implements BiomeModifier
{
    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
    {
        if (phase == Phase.ADD && useConfigs)
        {
            Collection<SpawnBiomeData> spawns = ConfigSettings.ENTITY_SPAWN_BIOMES.get(RegistryHelper.getRegistryAccess()).get(biome);
            for (SpawnBiomeData spawn : spawns)
            {
                RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ENTITY_TYPE, spawn.entities())
                .forEach(entityType ->
                {
                    MobSpawnSettings.SpawnerData spawnerData = FunctionalSpawnerData.create(entityType, spawn.count().min(), spawn.count().max(),
                                                                                 (level, structureManager, chunkGenerator, category, data, pos) ->
                                                                                 {
                                                                                     return spawn.location().test(req -> req.test(level, pos));
                                                                                 });
                    builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).removeIf(oldData -> oldData.value().type() == entityType);
                    builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, spawn.weight(), spawnerData);
                });
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec()
    {   return ModBiomeModifiers.ADD_SPAWNS_CODEC.value();
    }
}
