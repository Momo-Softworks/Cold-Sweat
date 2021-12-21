package net.momostudios.coldsweat.core.util;

import net.minecraft.util.math.BlockPos;

public class MathHelperCS
{
    /**
     * Converts a double temperature to a different unit. If {@code from} and {@code to} are the same, returns {@code value}.<br>
     * @param value The temperature to convert.
     * @param from The unit to convert from.
     * @param to The unit to convert to.
     * @param absolute Used when dealing with ambient temperatures with Minecraft units.
     * @return The converted temperature.
     */
    public static double convertUnits(double value, Units from, Units to, boolean absolute)
    {
        switch (from)
        {
            case C:
                switch (to)
                {
                    case C: return value;
                    case F: return value * 1.8 + 32d;
                    case MC: return value / 23.333333333d;
                }
            case F:
                switch (to)
                {
                    case C: return (value - 32) / 1.8;
                    case F: return value;
                    case MC: return (value - (absolute ? 32d : 0d)) / 42d;
                }
            case MC:
                switch (to)
                {
                    case C: return value * 23.333333333d;
                    case F: return value * 42d + (absolute ? 32d : 0d);
                    case MC: return value;
                }
            default: return value;
        }
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
