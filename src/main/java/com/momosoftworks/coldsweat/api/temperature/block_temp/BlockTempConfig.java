package com.momosoftworks.coldsweat.api.temperature.block_temp;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

public abstract class BlockTempConfig extends BlockTemp
{
    Map<String, Predicate<BlockState>> predicates;

    public BlockTempConfig(Map<String, Predicate<BlockState>> predicates, Block... blocks)
    {   super(blocks);
        this.predicates = predicates;
    }

    @Override
    public boolean isValid(World level, BlockPos pos, BlockState state)
    {
        if (this.predicates.isEmpty()) return true;

        for (Predicate<BlockState> predicate : predicates.values())
        {   if (!predicate.test(state)) return false;
        }
        return true;
    }

    public boolean comparePredicates(BlockTempConfig other)
    {   return predicates.keySet().equals(other.predicates.keySet());
    }

    public Collection<String> getPredicates()
    {   return predicates.keySet();
    }
}
