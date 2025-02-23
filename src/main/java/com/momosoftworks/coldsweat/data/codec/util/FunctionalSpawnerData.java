package com.momosoftworks.coldsweat.data.codec.util;


import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;

public class FunctionalSpawnerData extends MobSpawnInfo.Spawners
{
    private final SpawnFunction spawnFunction;

    public FunctionalSpawnerData(EntityType<?> entityType, int weight, int min, int max, SpawnFunction spawnFunction)
    {   super(entityType, weight, min, max);
        this.spawnFunction = spawnFunction;
    }

    public boolean canSpawn(ServerWorld level, StructureManager structureManager, ChunkGenerator chunkGenerator, EntityClassification category,
                            MobSpawnInfo.Spawners spawnerData, BlockPos pos)
    {   return spawnFunction == null || spawnFunction.canSpawn(level, structureManager, chunkGenerator, category, spawnerData, pos);
    }

    @FunctionalInterface
    public interface SpawnFunction
    {   boolean canSpawn(ServerWorld level, StructureManager structureManager, ChunkGenerator chunkGenerator, EntityClassification category,
                         MobSpawnInfo.Spawners spawnerData, BlockPos pos);
    }
}
