package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.BlockChangedEvent;
import dev.momostudios.coldsweat.common.event.BlockUpdateLimiter;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
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
@Mixin(ServerLevel.class)
public class MixinBlockUpdate
{
    ServerLevel level = (ServerLevel) (Object) this;

    @Inject(method = "onBlockStateChange", at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS)
    private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
    {
        if (BlockUpdateLimiter.UPDATES_THIS_TICK < 10 && oldState.equals(newState))
        {
            TaskScheduler.scheduleServer(() ->
            {
                if (level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
                {
                    MinecraftForge.EVENT_BUS.post(new BlockChangedEvent(pos, oldState, newState, level));
                }
            }, 1);
            BlockUpdateLimiter.UPDATES_THIS_TICK++;
        }
    }
}
