package com.momosoftworks.coldsweat.api.spread_rule;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Carries the two-sided context a {@link SpreadRule} needs to decide whether warmth
 * can flow from one block to a neighboring block, in a given direction.
 */
public record SpreadContext(Level level, BlockPos fromPos, BlockState fromState,
                             BlockPos toPos, BlockState toState,
                             Direction fromDirection, Direction toDirection)
{
    /**
     * Flips the context around the same pair of blocks, for handing a decision off to the other side's rule.
     */
    public SpreadContext reversed()
    {   return new SpreadContext(level, toPos, toState, fromPos, fromState, toDirection, toDirection.getOpposite());
    }
}
