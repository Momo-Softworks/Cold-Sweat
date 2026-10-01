package com.momosoftworks.coldsweat.data.codec.util;

import com.google.common.collect.MapMaker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * {@link MobSpawnSettings.SpawnerData} is a final record, so spawn conditions are attached to specific instances
 * via an identity-keyed weak map instead of by subclassing.
 */
public final class FunctionalSpawnerData
{
    private static final Map<MobSpawnSettings.SpawnerData, SpawnFunction> SPAWN_FUNCTIONS = new MapMaker().weakKeys().makeMap();

    private FunctionalSpawnerData() {}

    public static MobSpawnSettings.SpawnerData create(EntityType<?> entityType, int min, int max, @Nullable SpawnFunction spawnFunction)
    {
        MobSpawnSettings.SpawnerData spawnerData = new MobSpawnSettings.SpawnerData(entityType, min, max);
        if (spawnFunction != null)
        {   SPAWN_FUNCTIONS.put(spawnerData, spawnFunction);
        }
        return spawnerData;
    }

    public static boolean canSpawn(ServerLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory category,
                                   MobSpawnSettings.SpawnerData spawnerData, BlockPos pos)
    {
        SpawnFunction spawnFunction = SPAWN_FUNCTIONS.get(spawnerData);
        return spawnFunction == null || spawnFunction.canSpawn(level, structureManager, chunkGenerator, category, spawnerData, pos);
    }

    @FunctionalInterface
    public interface SpawnFunction
    {   boolean canSpawn(ServerLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory category,
                         MobSpawnSettings.SpawnerData spawnerData, BlockPos pos);
    }
}
