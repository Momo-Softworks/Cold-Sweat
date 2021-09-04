package net.momostudios.coldsweat.common.temperature.modifier.block;

import net.minecraft.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEffectEntries
{
    static BlockEffectEntries master = new BlockEffectEntries();
    static List<BlockEffect> entries = new ArrayList<>();

    public static BlockEffectEntries getEntries()
    {
        return master;
    }

    public static void add(BlockEffect blockEffect)
    {
        entries.add(blockEffect);
    }

    public static void remove(BlockEffect blockEffect)
    {
        entries.remove(blockEffect);
    }

    public static void flush()
    {
        entries.clear();
    }

    @Nullable
    public BlockEffect getEntryFor(Block block)
    {
        for (BlockEffect entry : entries)
        {
            if (entry.hasBlock(block.getDefaultState())) return entry;
        }
        return null;
    }
}
