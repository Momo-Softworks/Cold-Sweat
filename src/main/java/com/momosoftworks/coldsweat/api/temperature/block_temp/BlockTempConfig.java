package com.momosoftworks.coldsweat.api.temperature.block_temp;

import com.momosoftworks.coldsweat.util.world.BlockState;
import net.minecraft.block.Block;

import java.util.function.Predicate;

public abstract class BlockTempConfig extends BlockTemp
{
    Predicate<Integer> predicate;

    public BlockTempConfig(Predicate<Integer> predicate, Block... blocks)
    {   super(blocks);
        this.predicate = predicate;
    }

    public boolean testPredicate(BlockState state)
    {   return predicate.test(state.getMeta());
    }

    public boolean comparePredicate(BlockTempConfig other)
    {   return this.predicate.equals(other.predicate);
    }

    public Predicate<Integer> getPredicate()
    {   return predicate;
    }
}
