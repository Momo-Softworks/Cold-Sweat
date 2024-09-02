package com.momosoftworks.coldsweat.util.world;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class SpreadPath
{
    public final Direction direction;
    public final BlockPos pos;
    public final int x, y, z;
    public boolean frozen = false;
    public BlockPos origin;

    public SpreadPath(BlockPos pos, Direction direction)
    {   this.direction = direction;
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public SpreadPath(BlockPos pos)
    {
        this(pos, Direction.UP);
    }

    public SpreadPath(int x, int y, int z, Direction direction)
    {
        this.direction = direction;
        this.pos = new BlockPos(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SpreadPath setOrigin(BlockPos origin)
    {   this.origin = origin;
        return this;
    }

    public SpreadPath offset(int x, int y, int z)
    {
        return new SpreadPath(this.x + x, this.y + y, this.z + z, this.direction);
    }

    public SpreadPath offset(Direction dir)
    {
        return offset(dir, 1);
    }

    public SpreadPath offset(Direction dir, int steps)
    {
        return offset(dir.getStepX() * steps, dir.getStepY() * steps, dir.getStepZ() * steps);
    }

    public SpreadPath offset(BlockPos pos)
    {
        return offset(pos.getX(), pos.getY(), pos.getZ());
    }

    public SpreadPath spreadTo(BlockPos pos)
    {   return this.spreadTo(pos, CSMath.getDirectionFrom(this.pos, pos));
    }

    public SpreadPath spreadTo(Direction dir)
    {   return this.spreadTo(this.pos.relative(dir), dir);
    }

    public SpreadPath spreadTo(BlockPos pos, Direction dir)
    {
        SpreadPath path = new SpreadPath(pos, dir);
        path.setOrigin(this.origin);
        return path;
    }

    public boolean withinDistance(Vec3i vector, double distance)
    {
        return distanceSq(vector.getX(), vector.getY(), vector.getZ()) < distance * distance;
    }

    public double distanceSq(double x, double y, double z)
    {
        double d1 = this.x - x;
        double d2 = this.y - y;
        double d3 = this.z - z;
        return d1 * d1 + d2 * d2 + d3 * d3;
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof SpreadPath
        && this.pos.equals(((SpreadPath) o).pos);
    }
}
