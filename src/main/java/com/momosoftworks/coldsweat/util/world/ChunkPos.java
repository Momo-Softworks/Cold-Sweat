package com.momosoftworks.coldsweat.util.world;


import net.minecraft.world.chunk.Chunk;

public class ChunkPos
{
    int x, z;

    public ChunkPos(int x, int z)
    {   this.x = x;
        this.z = z;
    }

    public ChunkPos(BlockPos pos)
    {   this.x = pos.getX() >> 4;
        this.z = pos.getZ() >> 4;
    }

    public ChunkPos(Chunk chunk)
    {   this.x = chunk.xPosition;
        this.z = chunk.zPosition;
    }

    public int getX()
    {   return x;
    }

    public int getZ()
    {   return z;
    }
}
