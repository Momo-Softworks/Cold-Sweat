package com.momosoftworks.coldsweat.common.block;

import com.momosoftworks.coldsweat.common.fluid.SlushFluid;
import com.momosoftworks.coldsweat.data.tag.ModFluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Supplier;

public class SlushLiquidBlock extends LiquidBlock
{
    public SlushLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties)
    {
        super(fluid, properties);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        if (entity.canFreeze())
        {   entity.setIsInPowderSnow(true);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        if (this.shouldFreeze(level, pos, state, true))
        {   return;
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
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
    private boolean shouldFreeze(Level level, BlockPos pos, BlockState state, boolean freezeOther)
    {
        if (this.getFluid().is(ModFluidTags.SLUSH))
        {
            FluidState slushState = level.getFluidState(pos);

            for (Direction direction : POSSIBLE_FLOW_DIRECTIONS)
            {
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