package dev.momostudios.coldsweat.common.world;

import net.minecraft.block.BlockState;
import dev.momostudios.coldsweat.common.temperature.modifier.block.BlockEffect;

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

    public List<BlockEffect> getList()
    {
        return new ArrayList<>(entries);
    }

    public void add(BlockEffect blockEffect)
    {
        entries.add(blockEffect);
    }

    public void remove(BlockEffect blockEffect)
    {
        entries.remove(blockEffect);
    }

    public void flush()
    {
        entries.clear();
    }

    @Nullable
    public BlockEffect getEntryFor(BlockState block)
    {
        for (BlockEffect blockEffect : getList())
        {
            if (blockEffect.hasBlock(block))
                return blockEffect;
        }
        return null;
    }
}
