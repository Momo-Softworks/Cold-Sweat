package com.momosoftworks.coldsweat.api.spread_rule.compat;

import com.momosoftworks.coldsweat.api.spread_rule.SpreadContext;
import com.momosoftworks.coldsweat.api.spread_rule.SpreadRule;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.GlassFluidPipeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;

public class CreatePipeSpreadRule implements SpreadRule
{
    @Override
    public boolean matches(BlockState state)
    {   return CompatManager.Create.isFluidPipe(state);
    }

    @Override
    public boolean isTransferMedium()
    {   return true;
    }

    @Override
    public boolean canSpreadTo(SpreadContext ctx)
    {
        BlockState fromState = ctx.fromState();
        if (fromState.getBlock() instanceof FluidPipeBlock)
        {   return fromState.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(ctx.toDirection()));
        }
        if (fromState.getBlock() instanceof GlassFluidPipeBlock)
        {   return fromState.getValue(RotatedPillarBlock.AXIS) == ctx.toDirection().getAxis();
        }
        if (fromState.getBlock() instanceof EncasedPipeBlock)
        {   return fromState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(ctx.toDirection()));
        }
        return false;
    }
}
