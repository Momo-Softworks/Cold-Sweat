package com.momosoftworks.coldsweat.api.registry;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTempConfig;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.BlockState;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class BlockTempRegistry
{
    public static final LinkedList<BlockTemp> BLOCK_TEMPS = new LinkedList<>();
    public static final HashMap<Block, List<BlockTemp>> MAPPED_BLOCKS = new HashMap<>();
    public static final BlockTemp DEFAULT_BLOCK_TEMP = new BlockTemp()
    {
        @Override
        public double getTemperature(World world, EntityLivingBase entity, BlockState state, BlockPos pos, double distance)
        {   return 0;
        }
    };

    public static void register(BlockTemp blockTemp)
    {
        blockTemp.getAffectedBlocks().forEach(block ->
        {
            List<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
            if (blockTemps != null)
            {
                if (blockTemp instanceof BlockTempConfig)
                {
                    BlockTempConfig cfg = (BlockTempConfig) blockTemp;
                    for (BlockTemp temp : blockTemps)
                    {
                        if (temp instanceof BlockTempConfig)
                        {
                            BlockTempConfig cfg2 = (BlockTempConfig) temp;
                            if (cfg2.comparePredicate(cfg))
                            {   ColdSweat.LOGGER.error("Skipping duplicate BlockTemp for \"" + block.getLocalizedName() + "\" as it already has one with the same predicates: \n"
                                                               + cfg2.getPredicate());
                                return;
                            }
                        }
                    }
                }
                blockTemps.add(blockTemp);
            }
            else
            {   MAPPED_BLOCKS.put(block, new ArrayList<>(Arrays.asList(blockTemp)));
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
        if (blockstate.isAir()) return Arrays.asList(DEFAULT_BLOCK_TEMP);

        Block block = blockstate.getBlock();
        List<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
        if (blockTemps == null)
        {   blockTemps = BLOCK_TEMPS.stream().filter(bt -> bt.hasBlock(block)).collect(Collectors.toList());
            MAPPED_BLOCKS.put(block, blockTemps);
            return blockTemps;
        }
        return blockTemps;
    }
}
