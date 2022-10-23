package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.common.BlockChangedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Called on the server side when a block state is changed.<br>
 * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * {@link net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent} is only called on break/place. <br>
 * It doesn't trigger when a state is updated, nor does it know what the previous state was.<br>
 */
@Mixin(ServerLevel.class)
public class MixinBlockUpdate
{
    @Inject(method = "onBlockStateChange", at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS)
    private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
    {
        if (oldState != newState)
        {
            MinecraftForge.EVENT_BUS.post(new BlockChangedEvent(pos, oldState, newState, (ServerLevel) (Object) this));
        }
    }
}
