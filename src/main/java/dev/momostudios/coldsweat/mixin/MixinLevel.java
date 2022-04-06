package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class MixinLevel
{
    Level level = (Level) (Object) this;

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"),
    remap = ColdSweat.remapMixins, cancellable = true)
    public void setBlock(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> info)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            for (Direction value : Direction.values())
            {
                if (level.getBlockState(CSMath.offsetDirection(pos, value)).getBlock().equals(Blocks.CAVE_AIR) && !level.canSeeSky(pos))
                {
                    level.setBlock(pos, Blocks.CAVE_AIR.defaultBlockState(), 2);
                    info.cancel();
                }
                break;
            }
        }
    }
}
