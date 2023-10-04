package com.momosoftworks.coldsweat.util.world;

import com.momosoftworks.coldsweat.util.math.Direction;
import net.minecraft.util.AxisAlignedBB;

public class VoxelShape
{
    public double minX, minY, minZ, maxX, maxY, maxZ;

    public VoxelShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {   this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public VoxelShape(AxisAlignedBB aabb)
    {   this(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public static VoxelShape empty()
    {   return new VoxelShape(0, 0, 0, 0, 0, 0);
    }

    public static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {   return new VoxelShape(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isFullCube()
    {   return minX == 0 && minY == 0 && minZ == 0 && maxX == 1 && maxY == 1 && maxZ == 1;
    }

    public AxisAlignedBB getFaceShape(Direction direction)
    {   return getFaceShape(direction.getAxis());
    }

    public AxisAlignedBB getFaceShape(Direction.Axis axis)
    {
        switch (axis)
        {   case X:
                return AxisAlignedBB.getBoundingBox(minX, minY, minY, minX, maxY, maxY);
            case Y:
                return AxisAlignedBB.getBoundingBox(minX, minY, minY, maxX, minY, maxY);
            case Z:
                return AxisAlignedBB.getBoundingBox(minX, minY, minY, maxX, maxY, minY);
            default:
                return null;
        }
    }
}
