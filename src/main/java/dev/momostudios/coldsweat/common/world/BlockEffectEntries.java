package dev.momostudios.coldsweat.common.world;

import dev.momostudios.coldsweat.api.temperature.modifier.block.BlockEffect;
import net.minecraft.world.level.block.state.BlockState;

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

    /**
     * Do not edit this list directly. Use the add and remove methods instead.
     */
    public List<BlockEffect> getList()
    {
        return entries;
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
