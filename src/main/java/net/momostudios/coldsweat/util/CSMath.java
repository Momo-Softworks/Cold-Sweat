package net.momostudios.coldsweat.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class CSMath
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

    /**
     * Returns a number between the two given values {@code pointA} and {@code pointB}, based on factor.<br>
     * If {@code factor} = 0, returns {@code pointA}. If {@code factor} = {@code range}, returns {@code pointB}.<br>
     * @param pointA The first value.
     * @param pointB The second value.
     * @param factor The "progress" between pointA and pointB.
     * @param rangeMin The minimum of the range of values over which to interpolate.
     * @param rangeMax The maximum of the range of values over which to interpolate.
     * @return
     */
    public static double blend(double pointA, double pointB, double factor, double rangeMin, double rangeMax) {
        return ((1 / (rangeMax - rangeMin)) * (clamp(factor, rangeMin, rangeMax) - rangeMin)) * (pointB - pointA) + pointA;
    }

    public static double getDistance(Entity entity, BlockPos pos) {
        double xDistance = Math.max(0, Math.abs(entity.getPosX() - pos.getX()) - entity.getWidth() / 2);
        double yDistance = Math.max(0, Math.abs((entity.getPosY() + entity.getHeight() / 2) - pos.getY()) - entity.getHeight() / 2);
        double zDistance = Math.max(0, Math.abs(entity.getPosZ() - pos.getZ()) - entity.getWidth() / 2);
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }
}
