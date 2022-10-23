package dev.momostudios.coldsweat.util.world;

import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class SpreadPath
{
    Direction direction;
    BlockPos pos;
    boolean frozen = false;
    int step = 0;

    public SpreadPath(BlockPos pos)
    {
        this.pos = pos;
        this.direction = Direction.UP;
    }

    public SpreadPath(BlockPos pos, Direction direction)
    {
        this.direction = direction;
        this.pos = pos;
    }

    public SpreadPath(int x, int y, int z, Direction direction)
    {
        this.direction = direction;
        this.pos = new BlockPos(x, y, z);
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public boolean isFrozen()
    {
        return this.frozen;
    }

    public void freeze()
    {
        this.frozen = true;
    }

    public int getX()
    {
        return pos.getX();
    }
    public int getY()
    {
        return pos.getY();
    }
    public int getZ()
    {
        return pos.getZ();
    }

    public int getStep()
    {
        return this.step;
    }

    public SpreadPath offset(int x, int y, int z)
    {
        SpreadPath path = new SpreadPath(this.getX() + x, this.getY() + y, this.getZ() + z, this.direction);
        path.step = this.step + (int) CSMath.getDistance(pos, path.pos);
        return path;
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

    public boolean withinDistance(Vec3i vector, double distance)
    {
        return distanceSq(vector.getX(), vector.getY(), vector.getZ()) < distance * distance;
    }

    public double distanceSq(double x, double y, double z)
    {
        double d1 = this.getX() - x;
        double d2 = this.getY() - y;
        double d3 = this.getZ() - z;
        return d1 * d1 + d2 * d2 + d3 * d3;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpreadPath that = (SpreadPath) o;

        return pos.equals(that.pos);
    }
}
