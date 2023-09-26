package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.common.BlockStateChangedEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Called when a block state is changed.<br>
 * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * Updates must be delayed by 1 tick to prevent chunk deadlocking.
 */
@Mixin(ServerWorld.class)
public class MixinBlockUpdate
{
    ServerWorld level = (ServerWorld) (Object) this;

    @Inject(method = "onBlockStateChange", at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS)
    private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
    {
        if (!oldState.equals(newState))
        {   level.getServer().execute(() -> MinecraftForge.EVENT_BUS.post(new BlockStateChangedEvent(pos, level, oldState, newState)));
        }
    }
}