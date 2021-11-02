package net.momostudios.coldsweat.core.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class MathHelperCS
{
    public static double convertToF(double value)
    {
        return value * 42 + 32;
    }

    public static double convertToC(double value)
    {
        return (value * 210) / 9.0;
    }

    public static double convertFromC(double value)
    {
        return (value * 9.0) / 210;
    }

    public static double convertFromF(double value)
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

    public static boolean isEvenPosition(BlockPos pos) {
        return pos.getX() % 2 == 0 && pos.getY() % 2 == 0 && pos.getZ() % 2 == 0;
    }

    public static float toRadians(float input) {
        return input * (float) (Math.PI / 180);
    }

    public static float toDegrees(float input) {
        return input * (float) (180 / Math.PI);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
