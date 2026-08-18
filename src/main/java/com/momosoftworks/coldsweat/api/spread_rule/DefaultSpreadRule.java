package com.momosoftworks.coldsweat.api.spread_rule;

import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.block.BlockState;

/**
 * Fallback rule for plain open air/solid blocks. Must be registered last, as it matches everything.
 */
public class DefaultSpreadRule implements SpreadRule
{
    @Override
    public boolean matches(BlockState state)
    {   return true;
    }

    @Override
    public boolean canSpreadTo(SpreadContext ctx)
    {   return !WorldHelper.isSpreadBlocked(ctx.level(), ctx.fromState(), ctx.fromPos(), ctx.fromDirection(), ctx.toDirection());
    }
}
