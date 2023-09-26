package com.momosoftworks.coldsweat.util.serialization;

import net.minecraft.advancements.criterion.MinMaxBounds;

public interface FloatAtMost
{
    MinMaxBounds.FloatBound atMost(Float max);
}
