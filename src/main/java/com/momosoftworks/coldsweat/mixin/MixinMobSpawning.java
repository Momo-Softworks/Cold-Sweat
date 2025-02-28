package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.data.codec.util.FunctionalSpawnerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class MixinMobSpawning
{
    @Inject(method = "canSpawnMobAt",
            at = @At("RETURN"), cancellable = true)
    private static void checkFunctionalSpawnerData(ServerLevel level, StructureFeatureManager structureManager, ChunkGenerator chunkGenerator, MobCategory category,
                                                   MobSpawnSettings.SpawnerData spawnerData, BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        boolean canSpawn = cir.getReturnValue();
        if (spawnerData instanceof FunctionalSpawnerData functional)
        {   canSpawn = canSpawn && functional.canSpawn(level, structureManager, chunkGenerator, category, spawnerData, pos);
        }
        cir.setReturnValue(canSpawn);
    }
}
