package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.block_effect.BlockEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;

public class BlockEffectRegistry
{
    public static final HashSet<BlockEffect> BLOCK_EFFECTS = new HashSet<>();
    public static final HashMap<Block, BlockEffect> MAPPED_BLOCKS = new HashMap<>();
    public static final BlockEffect DEFAULT_BLOCK_EFFECT = new BlockEffect() {
        @Override
        public double getTemperature(Player player, BlockState state, BlockPos pos, double distance)
        {
            return 0;
        }
    };

    public static void register(BlockEffect blockEffect)
    {
        blockEffect.validBlocks.forEach(block ->
        {
            if (MAPPED_BLOCKS.containsKey(block))
            {
                ColdSweat.LOGGER.error("Block \"{}\" already has a registered BlockEffect ({})! Skipping BlockEffect {}...",
                        block.getRegistryName().toString(), MAPPED_BLOCKS.get(block).getClass().getSimpleName(), blockEffect.getClass().getSimpleName());
            }
            else
            {
                MAPPED_BLOCKS.put(block, blockEffect);
            }
        });
        BLOCK_EFFECTS.add(blockEffect);
    }

    public static void flush()
    {
        MAPPED_BLOCKS.clear();
    }

    public static BlockEffect getEntryFor(BlockState blockstate)
    {
        Block block = blockstate.getBlock();
        BlockEffect mappedEffect = MAPPED_BLOCKS.get(block);

        if (mappedEffect != null) return mappedEffect;
        else
        {
            if (blockstate.isAir())
            {
                MAPPED_BLOCKS.put(block, DEFAULT_BLOCK_EFFECT);
                return DEFAULT_BLOCK_EFFECT;
            }
            for (BlockEffect blockEffect : BLOCK_EFFECTS)
            {
                if (blockEffect.hasBlock(block))
                {
                    MAPPED_BLOCKS.put(block, blockEffect);
                    return blockEffect;
                }
            }

            MAPPED_BLOCKS.put(block, DEFAULT_BLOCK_EFFECT);
            return DEFAULT_BLOCK_EFFECT;
        }
    }
}
