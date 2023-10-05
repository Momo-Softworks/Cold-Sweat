package com.momosoftworks.coldsweat.util.math;

import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Vec3i
{
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);

    protected int x, y, z;

    public Vec3i(int x, int y, int z)
    {   this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(double x, double y, double z)
    {   this.x = MathHelper.floor_double(x);
        this.y = MathHelper.floor_double(y);
        this.z = MathHelper.floor_double(z);
    }

    public Vec3i(Vec3 vec3)
    {   this(vec3.xCoord, vec3.yCoord, vec3.zCoord);
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

    public double distSqr(Vec3i other)
    {   return (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z);
    }
}
