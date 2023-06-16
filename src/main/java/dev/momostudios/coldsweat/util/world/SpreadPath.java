package dev.momostudios.coldsweat.util.world;

import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.HashSet;
import java.util.Set;

public class SpreadPath
{
    private final Direction direction;
    private final BlockPos pos;
    private final int x, y, z;
    private boolean frozen = false;
    private final HashSet<SpreadPath> children = new HashSet<>();
    private BlockPos origin;

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
    public void setFrozen(boolean frozen)
    {
        this.frozen = frozen;
    }

    public int getX()
    {   return x;
    }
    public int getY()
    {   return y;
    }
    public int getZ()
    {   return z;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public BlockPos getOrigin()
    {   return origin;
    }

    public SpreadPath setOrigin(BlockPos origin)
    {   this.origin = origin;
        return this;
    }

    public Set<SpreadPath> getAllChildren()
    {
        Set<SpreadPath> allChildren = new HashSet<>();
        for (SpreadPath child : children)
        {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    public SpreadPath addChild(SpreadPath child)
    {
        this.children.add(child);
        return this;
    }

    public void clearChildren()
    {
        this.children.clear();
    }

    public SpreadPath offset(int x, int y, int z)
    {
        return new SpreadPath(this.getX() + x, this.getY() + y, this.getZ() + z, this.direction);
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
        path.setOrigin(this.getOrigin());
        this.addChild(path);
        return path;
    }

    public SpreadPath spreadTo(SpreadPath path)
    {
        this.addChild(path);
        return path;
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
        return o instanceof SpreadPath
        && this.pos == ((SpreadPath) o).pos;
    }
}
