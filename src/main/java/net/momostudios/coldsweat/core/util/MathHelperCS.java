package net.momostudios.coldsweat.core.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class MathHelperCS
{
    public static double MCtoF(double value)
    {
        return value * 42 + 32;
    }

    public static double MCtoC(double value)
    {
        return (value * 210) / 9.0;
    }

    public static double CtoMC(double value)
    {
        return (value * 9.0) / 210;
    }

    public static double FtoMC(double value)
    {
        return (value - 32) / 42.0;
    }

    public static double FtoC(double value)
    {
        return ((value - 32) * 5) / 9.0;
    }

    public static double CtoF(double value)
    {
        return value * (9.0 / 5) + 32;
    }

    public static boolean isEvenPosition(BlockPos pos, boolean xOffset, boolean yOffset, boolean zOffset) {
        return (xOffset == (pos.getX() % 2 == 0)) &&
               (yOffset == (pos.getY() % 2 == 0)) &&
               (zOffset == (pos.getZ() % 2 == 0));
    }

    public static float toRadians(float input) {
        return input * (float) (Math.PI / 180);
    }

    public static float toDegrees(float input) {
        return input * (float) (180 / Math.PI);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static boolean isBetween(Number value, Number min, Number max) {
        return value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue();
    }
}
