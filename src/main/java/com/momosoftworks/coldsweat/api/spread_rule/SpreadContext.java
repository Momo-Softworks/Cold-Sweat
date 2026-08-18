package com.momosoftworks.coldsweat.api.spread_rule;


import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * Carries the two-sided context a {@link SpreadRule} needs to decide whether warmth
 * can flow from one block to a neighboring block, in a given direction.
 */
public final class SpreadContext
{
    private final World level;
    private final BlockPos fromPos;
    private final BlockState fromState;
    private final BlockPos toPos;
    private final BlockState toState;
    private final Direction fromDirection;
    private final Direction toDirection;

    /**
     *
     */
    public SpreadContext(World level, BlockPos fromPos, BlockState fromState,
                         BlockPos toPos, BlockState toState,
                         Direction fromDirection, Direction toDirection)
    {
        this.level = level;
        this.fromPos = fromPos;
        this.fromState = fromState;
        this.toPos = toPos;
        this.toState = toState;
        this.fromDirection = fromDirection;
        this.toDirection = toDirection;
    }

    /**
     * Flips the context around the same pair of blocks, for handing a decision off to the other side's rule.
     */
    public SpreadContext reversed()
    {   return new SpreadContext(level, toPos, toState, fromPos, fromState, toDirection, toDirection.getOpposite());
    }

    public World level()
    {   return level;
    }
    public BlockPos fromPos()
    {   return fromPos;
    }
    public BlockState fromState()
    {   return fromState;
    }
    public BlockPos toPos()
    {   return toPos;
    }
    public BlockState toState()
    {   return toState;
    }
    public Direction fromDirection()
    {   return fromDirection;
    }
    public Direction toDirection()
    {   return toDirection;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SpreadContext that = (SpreadContext) obj;
        return Objects.equals(this.level, that.level) &&
            Objects.equals(this.fromPos, that.fromPos) &&
            Objects.equals(this.fromState, that.fromState) &&
            Objects.equals(this.toPos, that.toPos) &&
            Objects.equals(this.toState, that.toState) &&
            Objects.equals(this.fromDirection, that.fromDirection) &&
            Objects.equals(this.toDirection, that.toDirection);
    }

    @Override
    public int hashCode()
    {   return Objects.hash(level, fromPos, fromState, toPos, toState, fromDirection, toDirection);
    }
}
