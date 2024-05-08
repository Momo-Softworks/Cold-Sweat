package com.momosoftworks.coldsweat.api.registry;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTempConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BlockTempRegistry
{
    public static final LinkedList<BlockTemp> BLOCK_TEMPS = new LinkedList<>();
    public static final HashMap<Block, List<BlockTemp>> MAPPED_BLOCKS = new HashMap<>();
    public static final BlockTemp DEFAULT_BLOCK_TEMP = new BlockTemp()
    {
        @Override
        public double getTemperature(Level level, LivingEntity entity, BlockState state, BlockPos pos, double distance)
        {   return 0;
        }
    };

    public static void register(BlockTemp blockTemp)
    {
        blockTemp.getAffectedBlocks().forEach(block ->
        {
            // Get if this block is already registered
            List<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
            if (blockTemps != null)
            {
                if (blockTemp instanceof BlockTempConfig cfg)
                {
                    for (BlockTemp temp : blockTemps)
                    {
                        if (temp instanceof BlockTempConfig cfg2)
                        {   if (cfg2.comparePredicates(cfg))
                        {
                            ColdSweat.LOGGER.error("Skipping duplicate BlockTemp for \"{}\" as it already has one with the same predicates: \n{}",
                                                   block.getName().getString(), cfg2.getPredicates());
                            return;
                        }
                        }
                    }
                }
                blockTemps.add(blockTemp);
            }
            else
            {   MAPPED_BLOCKS.put(block, new ArrayList<>(List.of(blockTemp)));
            }
        });
        BLOCK_TEMPS.add(blockTemp);
    }

    public static void flush()
    {
        MAPPED_BLOCKS.clear();
    }

    public static List<BlockTemp> getBlockTempsFor(BlockState blockstate)
    {
        if (blockstate.isAir()) return List.of(DEFAULT_BLOCK_TEMP);

        Block block = blockstate.getBlock();
        List<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
        if (blockTemps == null)
        {   blockTemps = new ArrayList<>(BLOCK_TEMPS.stream().filter(bt -> bt.hasBlock(block)).toList());
            MAPPED_BLOCKS.put(block, blockTemps);
            return blockTemps;
        }
        return blockTemps;
    }
}
