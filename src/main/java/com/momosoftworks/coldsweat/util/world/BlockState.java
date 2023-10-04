package com.momosoftworks.coldsweat.util.world;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BlockState
{
    Block block;
    int meta;

    public BlockState(Block block, int meta)
    {   this.block = block;
        this.meta = meta;
    }

    public Block getBlock()
    {   return block;
    }

    public int getMeta()
    {   return meta;
    }

    public boolean isAir()
    {   return block == Blocks.air;
    }

    public static BlockState of(Block block, int meta)
    {   return new BlockState(block, meta);
    }
}
