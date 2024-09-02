package com.momosoftworks.coldsweat.mixin.compat;

import com.momosoftworks.coldsweat.core.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO: Reimplement when Create is updated
//@Mixin(FluidPipeBlock.class)
public class MixinCreateConnect
{
    /**
     * Enable Create pipes connecting to the smokestack of the hearth
     */
    /*@Inject(method = "canConnectTo", at = @At("HEAD"), cancellable = true, remap = false)
    private static void shouldPipesConnectTo(BlockAndTintGetter world, BlockPos neighborPos, BlockState neighbor, Direction direction, CallbackInfoReturnable<Boolean> cir)
    {
        if (direction == Direction.DOWN)
        {
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.is(ModBlocks.HEARTH_TOP))
            {   cir.setReturnValue(true);
            }
        }
    }*/
}
