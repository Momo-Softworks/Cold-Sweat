package com.momosoftworks.coldsweat.util.world;

import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Direction;
import com.momosoftworks.coldsweat.util.math.Vec3i;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class BlockPos extends Vec3i
{
    public BlockPos(int x, int y, int z)
    {   super(x, y, z);
    }

    public BlockPos(double x, double y, double z)
    {   super(x, y, z);
    }

    public BlockPos(Vec3 vec3)
    {   super(vec3);
    }

    public BlockPos(Entity entity)
    {   this(entity.posX, entity.posY, entity.posZ);
    }

    public Mutable mutable()
    {   return new Mutable(x, y, z);
    }

    public static class Mutable extends BlockPos
    {
        public Mutable(int x, int y, int z)
        {   super(x, y, z);
        }

        public Mutable()
        {   this(0, 0, 0);
        }

        public void set(int x, int y, int z)
        {   this.x = x;
            this.y = y;
            this.z = z;
        }

        public void set(double x, double y, double z)
        {   this.x = CSMath.floor(x);
            this.y = CSMath.floor(y);
            this.z = CSMath.floor(z);
        }

        public void set(BlockPos pos)
        {   this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }

        public void move(int x, int y, int z)
        {   this.x += x;
            this.y += y;
            this.z += z;
        }

        public BlockPos immutable()
        {   return new BlockPos(x, y, z);
        }
    }

    public BlockPos up(int amount) {
        return new BlockPos(x, y + amount, z);
    }

    public BlockPos up() {
        return up(1);
    }

    public BlockPos down(int amount) {
        return new BlockPos(x, y - amount, z);
    }

    public BlockPos down() {
        return down(1);
    }

    public BlockPos north(int amount) {
        return new BlockPos(x, y, z - amount);
    }

    public BlockPos north() {
        return north(1);
    }

    public BlockPos south(int amount) {
        return new BlockPos(x, y, z + amount);
    }

    public BlockPos south() {
        return south(1);
    }

    public BlockPos east(int amount) {
        return new BlockPos(x + amount, y, z);
    }

    public BlockPos east() {
        return east(1);
    }

    public BlockPos west(int amount) {
        return new BlockPos(x - amount, y, z);
    }

    public BlockPos west() {
        return west(1);
    }

    public BlockPos add(int x, int y, int z)
    {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(double x, double y, double z)
    {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public BlockPos add(Vec3 vec3)
    {
        return new BlockPos(this.x + vec3.xCoord, this.y + vec3.yCoord, this.z + vec3.zCoord);
    }

    public BlockPos offset(Direction direction)
    {
        return new BlockPos(this.x + direction.getNormal().getX(), this.y + direction.getNormal().getY(), this.z + direction.getNormal().getZ());
    }

    public BlockPos offset(int x, int y, int z)
    {
        return new BlockPos(this.x + x, this.y + y, this.z + z);
    }

    public double distance(BlockPos pos2)
    {
        return Math.sqrt(Math.pow(pos2.x - this.x, 2) + Math.pow(pos2.y - this.y, 2) + Math.pow(pos2.z - this.z, 2));
    }

    @Override
    public String toString()
    {   return "BlockPos{" + x + ", " + y + ", " + z + "}";
    }
}
