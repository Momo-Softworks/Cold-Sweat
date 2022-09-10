package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.block_temp.BlockTemp;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.LinkedList;

public class BlockTempRegistry
{
    public static final LinkedList<BlockTemp> BLOCK_EFFECTS = new LinkedList<>();
    public static final HashMap<Block, BlockTemp> MAPPED_BLOCKS = new HashMap<>();
    public static final BlockTemp DEFAULT_BLOCK_EFFECT = new BlockTemp() {
        @Override
        public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
        {
            return 0;
        }
    };

    public static void register(BlockTemp blockTemp)
    {
        blockTemp.validBlocks.forEach(block ->
        {
            if (MAPPED_BLOCKS.containsKey(block))
            {
                ColdSweat.LOGGER.error("Block \"{}\" already has a registered BlockTemp ({})! Skipping BlockTemp {}...",
                        block.getRegistryName().toString(), MAPPED_BLOCKS.get(block).getClass().getSimpleName(), blockTemp.getClass().getSimpleName());
            }
            else
            {
                MAPPED_BLOCKS.put(block, blockTemp);
            }
        });
        BLOCK_EFFECTS.add(blockTemp);
    }

    public static void flush()
    {
        MAPPED_BLOCKS.clear();
    }

    public static BlockTemp getEntryFor(BlockState blockstate)
    {
        if (blockstate.isAir())
        {
            return DEFAULT_BLOCK_EFFECT;
        }

        return MAPPED_BLOCKS.computeIfAbsent(blockstate.getBlock(), (block) ->
        {
            for (BlockTemp blockTemp : BLOCK_EFFECTS)
            {
                if (blockTemp.hasBlock(block))
                {
                    return blockTemp;
                }
            }

            return DEFAULT_BLOCK_EFFECT;
        });
    }
}
