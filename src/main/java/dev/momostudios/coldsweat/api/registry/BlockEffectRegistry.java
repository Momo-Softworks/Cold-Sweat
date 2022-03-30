package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.temperature.block_effect.BlockEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockEffectRegistry
{
    private static final Map<Block, BlockEffect> MAPPED_BLOCKS = new HashMap<>();

    static BlockEffectRegistry REGISTER = new BlockEffectRegistry();

    public static BlockEffectRegistry getRegister()
    {
        return REGISTER;
    }

    public void register(BlockEffect blockEffect)
    {
        blockEffect.validBlocks.forEach(block ->
        {
            if (MAPPED_BLOCKS.containsKey(block))
            {
                ColdSweat.LOGGER.error("Block {} already has a registered BlockEffect ({})! Skipping BlockEffect {}...",
                                        block.getName().getString(), MAPPED_BLOCKS.get(block).getClass().getSimpleName(), blockEffect.getClass().getSimpleName());
            }
            MAPPED_BLOCKS.putIfAbsent(block, blockEffect);
        });
    }

    public static void flush()
    {
        MAPPED_BLOCKS.clear();
    }

    @Nullable
    public BlockEffect getEntryFor(BlockState block)
    {
        return MAPPED_BLOCKS.get(block.getBlock());
    }
}
