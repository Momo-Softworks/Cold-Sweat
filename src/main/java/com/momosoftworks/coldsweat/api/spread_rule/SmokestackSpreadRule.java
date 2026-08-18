package com.momosoftworks.coldsweat.api.spread_rule;

import com.momosoftworks.coldsweat.api.registry.SpreadRuleRegistry;
import com.momosoftworks.coldsweat.common.block.SmokestackBlock;
import net.minecraft.block.BlockState;

public class SmokestackSpreadRule implements SpreadRule
{
    @Override
    public boolean matches(BlockState state)
    {   return state.getBlock() instanceof SmokestackBlock;
    }

    @Override
    public boolean isTransferMedium()
    {   return true;
    }

    @Override
    public boolean canSpreadTo(SpreadContext ctx)
    {
        SmokestackBlock.Facing facing = ctx.fromState().getValue(SmokestackBlock.FACING);

        // Spreading from a junction: the target must also be some kind of transfer medium
        if (facing == SmokestackBlock.Facing.BEND)
        {
            SpreadRule toRule = SpreadRuleRegistry.get(ctx.toState());
            if (!toRule.isTransferMedium())
            {   return false;
            }
            if (toRule instanceof SmokestackSpreadRule)
            {
                SmokestackBlock.Facing toFacing = ctx.toState().getValue(SmokestackBlock.FACING);
                return toFacing == SmokestackBlock.Facing.BEND || toFacing.getAxis() == ctx.toDirection().getAxis();
            }
            // Hand off to the other medium's own rule (e.g. a Create pipe) to decide
            return toRule.canSpreadTo(ctx.reversed());
        }
        // Spreading from a directional smokestack
        return facing.getAxis() == ctx.toDirection().getAxis();
    }
}
