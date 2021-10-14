package net.momostudios.coldsweat.core.util;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MathHelperCS
{
    public static int convertToF(double value)
    {
        return (int) (value * 42 + 32);
    }

    public static int convertToC(double value)
    {
        return (int) ((value * 210) / 9.0);
    }

    public static int FtoC(int value)
    {
        return (int) (((value - 32) * 5) / 9.0);
    }

    public static boolean isEvenPosition(BlockPos pos)
    {
        return pos.getX() % 2 == 0 && pos.getY() % 2 == 0 && pos.getZ() % 2 == 0;
    }
}
