package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.data.codec.util.FunctionalSpawnerData;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldEntitySpawner.class)
public class MixinMobSpawning
{
    @Inject(method = "canSpawnMobAt",
            at = @At("RETURN"), cancellable = true)
    private static void checkFunctionalSpawnerData(ServerWorld level, StructureManager structureManager, ChunkGenerator chunkGenerator, EntityClassification category,
                                                   MobSpawnInfo.Spawners spawnerData, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        boolean canSpawn = cir.getReturnValue();
        if (spawnerData instanceof FunctionalSpawnerData)
        {   canSpawn = canSpawn && ((FunctionalSpawnerData) spawnerData).canSpawn(level, structureManager, chunkGenerator, category, spawnerData, pos);
        }
        cir.setReturnValue(canSpawn);
    }
}
