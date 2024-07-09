package com.momosoftworks.coldsweat.data.biome_modifier;

import com.mojang.serialization.MapCodec;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModBiomeModifiers;
import com.momosoftworks.coldsweat.data.codec.configuration.SpawnBiomeData;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
            Collection<SpawnBiomeData> spawns = ConfigSettings.ENTITY_SPAWN_BIOMES.get(RegistryHelper.getRegistryAccess()).get(biome.value());
            for (SpawnBiomeData spawn : spawns)
            {
                RegistryHelper.mapRegistryTagList(Registries.ENTITY_TYPE, spawn.entities(), RegistryHelper.getRegistryAccess())
                .forEach(entityType ->
                {
                    builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE).removeIf(spawnerData -> spawnerData.type == entityType);
                    builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(entityType, spawn.weight(), 1, 1));
                });
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec()
    {
        return ModBiomeModifiers.ADD_SPAWNS_CODEC.value();
    }
}
