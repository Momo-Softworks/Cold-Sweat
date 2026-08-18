package com.momosoftworks.coldsweat.api.spread_rule;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Governs how the Hearth's warmth spreads through a category of blocks (open air, Smokestacks,
 * Create fluid pipes, etc). Queried through {@link com.momosoftworks.coldsweat.api.registry.SpreadRuleRegistry}
 * instead of hard-coded block-type checks.
 */
public interface SpreadRule
{
    /**
     * Whether this rule governs the given block state.
     */
    boolean matches(BlockState state);

    /**
     * Whether positions matching this rule should keep trying to spread in every direction
     * regardless of skylight access (e.g. pipes), rather than stop once they see the sky.
     */
    default boolean isTransferMedium()
    {   return false;
    }

    /**
     * Whether warmth can continue from {@link SpreadContext#fromPos()} to {@link SpreadContext#toPos()}.
     */
    boolean canSpreadTo(SpreadContext ctx);
}
