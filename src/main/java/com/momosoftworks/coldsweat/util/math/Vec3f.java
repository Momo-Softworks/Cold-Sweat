package com.momosoftworks.coldsweat.util.math;

import net.minecraft.util.MathHelper;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3d;

public class Vec3f
{
    public static Vec3f XN = new Vec3f(-1.0F, 0.0F, 0.0F);
    public static Vec3f XP = new Vec3f(1.0F, 0.0F, 0.0F);
    public static Vec3f YN = new Vec3f(0.0F, -1.0F, 0.0F);
    public static Vec3f YP = new Vec3f(0.0F, 1.0F, 0.0F);
    public static Vec3f ZN = new Vec3f(0.0F, 0.0F, -1.0F);
    public static Vec3f ZP = new Vec3f(0.0F, 0.0F, 1.0F);
    private float x;
    private float y;
    private float z;

    public Vec3f() {
    }

    public Vec3f(float pX, float pY, float pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }

    public Vec3f(Vector3d pVec3) {
        this((float)pVec3.x, (float)pVec3.y, (float)pVec3.z);
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            Vec3f Vec3f = (Vec3f)p_equals_1_;
            if (Float.compare(Vec3f.x, this.x) != 0) {
                return false;
            } else if (Float.compare(Vec3f.y, this.y) != 0) {
                return false;
            } else {
                return Float.compare(Vec3f.z, this.z) == 0;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int i = Float.floatToIntBits(this.x);
        i = 31 * i + Float.floatToIntBits(this.y);
        return 31 * i + Float.floatToIntBits(this.z);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }

    public void mul(float pMultiplier) {
        this.x *= pMultiplier;
        this.y *= pMultiplier;
        this.z *= pMultiplier;
    }

    public void mul(float pMx, float pMy, float pMz) {
        this.x *= pMx;
        this.y *= pMy;
        this.z *= pMz;
    }

    public void clamp(float pMin, float pMax) {
        this.x = MathHelper.clamp_float(this.x, pMin, pMax);
        this.y = MathHelper.clamp_float(this.y, pMin, pMax);
        this.z = MathHelper.clamp_float(this.z, pMin, pMax);
    }

    public void set(float pX, float pY, float pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }

    public void add(float pX, float pY, float pZ) {
        this.x += pX;
        this.y += pY;
        this.z += pZ;
    }

    public void add(Vec3f pOther) {
        this.x += pOther.x;
        this.y += pOther.y;
        this.z += pOther.z;
    }

    public void sub(Vec3f pOther) {
        this.x -= pOther.x;
        this.y -= pOther.y;
        this.z -= pOther.z;
    }

    public float dot(Vec3f pOther) {
        return this.x * pOther.x + this.y * pOther.y + this.z * pOther.z;
    }

    public boolean normalize() {
        float f = this.x * this.x + this.y * this.y + this.z * this.z;
        if ((double)f < 1.0E-5D) {
            return false;
        } else {
            float f1 = MathHelper.sqrt_float(f);
            this.x *= f1;
            this.y *= f1;
            this.z *= f1;
            return true;
        }
    }

    public void cross(Vec3f pOther) {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        float f3 = pOther.x();
        float f4 = pOther.y();
        float f5 = pOther.z();
        this.x = f1 * f5 - f2 * f4;
        this.y = f2 * f3 - f * f5;
        this.z = f * f4 - f1 * f3;
    }

    public void transform(Matrix3f pMatrix) {
        float f = this.x;
        float f1 = this.y;
        float f2 = this.z;
        this.x = pMatrix.m00 * f + pMatrix.m01 * f1 + pMatrix.m02 * f2;
        this.y = pMatrix.m10 * f + pMatrix.m11 * f1 + pMatrix.m12 * f2;
        this.z = pMatrix.m20 * f + pMatrix.m21 * f1 + pMatrix.m22 * f2;
    }

    public void lerp(Vec3f pVector, float pDelta) {
        float f = 1.0F - pDelta;
        this.x = this.x * f + pVector.x * pDelta;
        this.y = this.y * f + pVector.y * pDelta;
        this.z = this.z * f + pVector.z * pDelta;
    }

    public Quaternion rotation(float pValue) {
        return new Quaternion(this, pValue, false);
    }

    public Quaternion rotationDegrees(float pValue) {
        return new Quaternion(this, pValue, true);
    }

    public Vec3f copy() {
        return new Vec3f(this.x, this.y, this.z);
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    // Forge start
    public Vec3f(float[] values) {
        set(values);
    }
    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
}
