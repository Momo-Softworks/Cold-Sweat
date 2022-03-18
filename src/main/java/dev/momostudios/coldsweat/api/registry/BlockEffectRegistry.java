package dev.momostudios.coldsweat.api.registry;

import dev.momostudios.coldsweat.api.temperature.block_effect.BlockEffect;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayDeque;

public class BlockEffectRegistry
{
    static BlockEffectRegistry register = new BlockEffectRegistry();
    static ArrayDeque<BlockEffect> entries = new ArrayDeque<>(64);

    public static BlockEffectRegistry getRegister()
    {
        return register;
    }

    public ArrayDeque<BlockEffect> getEntries()
    {
        return entries;
    }

    public void register(BlockEffect blockEffect)
    {
        entries.add(blockEffect);
    }

    public static void flush()
    {
        entries.clear();
    }

    @Nullable
    public BlockEffect getEntryFor(BlockState block)
    {
        for (BlockEffect blockEffect : getEntries())
        {
            if (blockEffect.hasBlock(block))
                return blockEffect;
        }
        return null;
    }
}
