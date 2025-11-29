package com.momosoftworks.coldsweat.api.registry;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.block_temp.BlockTemp;
import com.momosoftworks.coldsweat.api.temperature.block_temp.ConfiguredBlockTemp;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class BlockTempRegistry
{
    public static final List<BlockTemp> BLOCK_TEMPS = new ArrayList<>();
    public static final Multimap<Block, BlockTemp> MAPPED_BLOCKS = MultimapBuilder.hashKeys().linkedHashSetValues().build();
    public static final BlockTemp DEFAULT_BLOCK_TEMP = new BlockTemp()
    {
        @Override
        public double getTemperature(World world, LivingEntity entity, BlockState state, BlockPos pos, double distance)
        {   return 0;
        }
    };

    public static synchronized void register(BlockTemp blockTemp)
    {   register(blockTemp, false);
    }

    public static synchronized void registerFirst(BlockTemp blockTemp)
    {   register(blockTemp, true);
    }

    private static synchronized void register(BlockTemp blockTemp, boolean front)
    {
        blockTemp.getAffectedBlocks().forEach(block ->
        {
            Collection<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
            if (!blockTemps.isEmpty() && blockTemp instanceof ConfiguredBlockTemp)
            {
                ConfiguredBlockTemp cfg = (ConfiguredBlockTemp) blockTemp;
                for (BlockTemp temp : blockTemps)
                {
                    if (temp instanceof ConfiguredBlockTemp && cfg.equals(temp))
                    {   ColdSweat.LOGGER.error("Skipping duplicate BlockTemp for \"{}\" as an identical one is already registered", block);
                        ColdSweat.LOGGER.debug("{}", cfg);
                        return;
                    }
                }
            }
            if (front)
            {
                List<BlockTemp> blockTempList = new ArrayList<>(blockTemps);
                blockTempList.add(0, blockTemp);
                blockTemps.clear();
                blockTemps.addAll(blockTempList);
            }
            else blockTemps.add(blockTemp);
        });
        if (front) BLOCK_TEMPS.add(0, blockTemp);
        else BLOCK_TEMPS.add(blockTemp);
    }

    public static synchronized void flush()
    {
        MAPPED_BLOCKS.clear();
        BLOCK_TEMPS.clear();
    }

    public static Collection<BlockTemp> getBlockTempsFor(BlockState blockstate)
    {
        if (blockstate.isAir()) return Arrays.asList(DEFAULT_BLOCK_TEMP);

        Block block = blockstate.getBlock();
        Collection<BlockTemp> blockTemps = MAPPED_BLOCKS.get(block);
        if (blockTemps.isEmpty())
        {
            blockTemps = new ArrayList<>(BLOCK_TEMPS.stream().filter(bt -> bt.hasBlock(block)).collect(Collectors.toList()));
            // If this block has no associated BlockTemps, give default implementation
            if (blockTemps.isEmpty())
            {   blockTemps.add(DEFAULT_BLOCK_TEMP);
            }
            MAPPED_BLOCKS.putAll(block, blockTemps);
            return blockTemps;
        }
        return blockTemps;
    }

    public static Optional<BlockTemp> getFirstBlockTempFor(BlockState blockstate, World level, BlockPos pos)
    {
        Collection<BlockTemp> blockTemps = getBlockTempsFor(blockstate);
        return blockTemps.stream().filter(temp -> temp.isValid(level, pos, blockstate)).findFirst();
    }
}
