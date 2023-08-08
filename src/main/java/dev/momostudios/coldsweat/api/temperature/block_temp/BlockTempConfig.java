package dev.momostudios.coldsweat.api.temperature.block_temp;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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

    public boolean testPredicates(BlockState state)
    {
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
