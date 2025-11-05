package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import com.momosoftworks.coldsweat.data.tag.ModFluidTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Supplier;

public class SlushLiquidBlock extends FlowingFluidBlock
{
    public SlushLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties)
    {
        super(fluid, properties);
    }

    @Override
    public void onPlace(BlockState state, World level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        if (this.shouldFreeze(level, pos, state, true))
        {   return;
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        if (this.shouldFreeze(level, pos, state, false))
        {   return;
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    /**
     * Checks if water is adjacent to this slush block and freezes if appropriate.
     * Returns true if the block was frozen (replaced).
     */
    private boolean shouldFreeze(World level, BlockPos pos, BlockState state, boolean freezeOther)
    {
        if (this.getFluid().is(ModFluidTags.SLUSH))
        {
            FluidState slushState = level.getFluidState(pos);

            for (Direction direction : Direction.values())
            {
                if (direction == Direction.UP) continue;
                BlockPos neighborPos = pos.relative(direction);
                FluidState neighborFluid = level.getFluidState(neighborPos);

                if (neighborFluid.is(FluidTags.WATER))
                {
                    Block resultBlock = slushState.isSource() ? Blocks.ICE : Blocks.SNOW_BLOCK;
                    BlockPos resultPos = freezeOther ? neighborPos : pos;
                    level.setBlockAndUpdate(resultPos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, resultPos, pos, resultBlock.defaultBlockState()));
                    SlushFluid.fizz(level, pos);
                    return true;
                }
            }
        }
        return false;
    }
}