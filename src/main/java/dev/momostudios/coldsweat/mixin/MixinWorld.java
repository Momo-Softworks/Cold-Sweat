package dev.momostudios.coldsweat.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.momostudios.coldsweat.util.WorldHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class MixinWorld
{
    World world = (World) (Object) this;

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
    public void setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> info)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            for (Direction value : Direction.values())
            {
                if (world.getBlockState(pos.offset(value)).getBlock().equals(Blocks.CAVE_AIR) && !world.canBlockSeeSky(pos))
                {
                    WorldHelper.schedule(() -> world.setBlockState(pos, Blocks.CAVE_AIR.getDefaultState()), 1);
                }
            }
        }
    }
}
