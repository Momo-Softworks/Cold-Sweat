package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.BlockStateChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Called when a block state is changed.<br>
 * This is not an {@link net.neoforged.bus.api.ICancellableEvent}.<br>
 * <br>
 * Updates must be delayed by 1 tick to prevent chunk deadlocking.
 */
@Mixin(ServerLevel.class)
public class MixinBlockUpdate
{
    ServerLevel level = (ServerLevel) (Object) this;

    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
    {
        if (!oldState.equals(newState))
        {   level.getServer().execute(() -> NeoForge.EVENT_BUS.post(new BlockStateChangedEvent(pos, level, oldState, newState)));
        }
    }
}