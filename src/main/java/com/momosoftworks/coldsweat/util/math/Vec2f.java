package com.momosoftworks.coldsweat.util.math;

public class Vec2f
{
    public float x, y;

    public Vec2f(float x, float y)
    {   this.x = x;
        this.y = y;
    }

    public float getX()
    {   return x;
    }

    public float getY()
    {   return y;
    }

    public Vec2f mul(float scalar)
    {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vec2f add(Vec2f other)
    {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public Vec2f sub(Vec2f other)
    {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public Vec2f div(float scalar)
    {
        this.x /= scalar;
        this.y /= scalar;
        return this;
    }
}
