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
    private boolean frozen = false;
    private final HashSet<SpreadPath> children = new HashSet<>();
    private BlockPos origin;

    public SpreadPath(BlockPos pos, Direction direction)
    {
        this.direction = direction;
        this.pos = pos;
        this.origin = pos;
    }

    public SpreadPath(BlockPos pos)
    {
        this(pos, Direction.UP);
    }

    public SpreadPath(int x, int y, int z, Direction direction)
    {
        this(new BlockPos(x, y, z), direction);
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

    public BlockPos getOrigin()
    {
        return this.origin;
    }
    public SpreadPath setOrigin(BlockPos origin)
    {
        this.origin = origin;
        return this;
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

    public Direction getDirection()
    {
        return direction;
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
    {
        SpreadPath path = new SpreadPath(pos, CSMath.getDirectionFrom(this.pos, pos));
        this.addChild(path);
        return path;
    }

    public SpreadPath spreadTo(Direction dir)
    {
        SpreadPath path = new SpreadPath(this.pos.relative(dir), dir);
        this.addChild(path.setOrigin(this.origin));
        return path;
    }

    public SpreadPath spreadTo(BlockPos pos, Direction dir)
    {
        SpreadPath path = new SpreadPath(pos, dir);
        this.addChild(path.setOrigin(this.origin));
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
        return o instanceof SpreadPath that
        && pos.getX() == that.pos.getX()
        && pos.getY() == that.pos.getY()
        && pos.getZ() == that.pos.getZ();
    }
}
