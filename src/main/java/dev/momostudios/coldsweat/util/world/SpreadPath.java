package dev.momostudios.coldsweat.util.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class SpreadPath
{
    public Direction direction;
    public BlockPos pos;
    public int freezes = 0;
    public boolean removable = false;

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

    public BlockPos blockPos()
    {
        return pos;
    }

    public SpreadPath offset(Direction dir)
    {
        return new SpreadPath(this.getX() + dir.getStepX(), this.getY() + dir.getStepY(), this.getZ() + dir.getStepZ(), dir);
    }

    public SpreadPath offset(Direction dir, int steps)
    {
        return new SpreadPath(this.getX() + dir.getStepX() * steps, this.getY() + dir.getStepY() * steps, this.getZ() + dir.getStepZ() * steps, dir);
    }

    public SpreadPath offset(int x, int y, int z)
    {
        return new SpreadPath(this.getX() + x, this.getY() + y, this.getZ() + z, this.direction);
    }

    public SpreadPath offset(BlockPos pos)
    {
        return new SpreadPath(this.getX() + pos.getX(), this.getY() + pos.getY(), this.getZ() + pos.getZ(), this.direction);
    }

    public boolean withinDistance(Vec3i vector, double distance) {
        return distanceSq(vector.getX(), vector.getY(), vector.getZ()) < distance * distance;
    }

    public double distanceSq(double x, double y, double z) {
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

        if (getX() != that.getX()) return false;
        if (getY() != that.getY()) return false;
        return getZ() == that.getZ();
    }
}
