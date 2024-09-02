package com.momosoftworks.coldsweat.mixin.compat;

import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import sereneseasons.api.season.Season;
import sereneseasons.handler.season.RandomUpdateHandler;

@Mixin(RandomUpdateHandler.class)
public class MixinSereneIceMelt
{
    @Inject(method = "meltInChunk",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;below()Lnet/minecraft/core/BlockPos;"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private static void getBiomeTemperatureOverride(ChunkMap chunkManager, LevelChunk chunkIn, Season.SubSeason subSeason, CallbackInfo ci,
                                                    //locals
                                                    ServerLevel level, ChunkPos chunkpos, int i, int j, int meltRand, BlockPos topAirPos)
    {
        BlockPos groundPos = topAirPos.below();
        if (WorldHelper.getWorldTemperatureAt(level, groundPos) < 0.15F)
        {
            ci.cancel();
        }
    }
}
