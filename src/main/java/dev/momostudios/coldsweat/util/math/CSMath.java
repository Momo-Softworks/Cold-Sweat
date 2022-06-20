package dev.momostudios.coldsweat.util.math;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public class CSMath
{
    /**
     * Converts a double temperature to a different unit. If {@code from} and {@code to} are the same, returns {@code value}.<br>
     * @param value The temperature to convert.
     * @param from The unit to convert from.
     * @param to The unit to convert to.
     * @param absolute Used when dealing with world temperatures with Minecraft units.
     * @return The converted temperature.
     */
    public static double convertUnits(double value, Temperature.Units from, Temperature.Units to, boolean absolute)
    {
        return switch (from)
        {
            case C -> switch (to)
            {
                case C -> value;
                case F -> value * 1.8 + 32d;
                case MC -> value / 23.333333333d;
            };
            case F -> switch (to)
            {
                case C -> (value - 32) / 1.8;
                case F -> value;
                case MC -> (value - (absolute ? 32d : 0d)) / 42d;
            };
            case MC -> switch (to)
            {
                case C -> value * 23.333333333d;
                case F -> value * 42d + (absolute ? 32d : 0d);
                case MC -> value;
            };
            default -> value;
        };
    }

    public static float toRadians(float input) {
        return input * (float) (Math.PI / 180);
    }

    public static float toDegrees(float input) {
        return input * (float) (180 / Math.PI);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Calculates if the given value is between two values (inclusive)
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Calculates if the given value is between two values (exclusive)
     */
    public static boolean isBetween(double value, double min, double max) {
        return value > min && value < max;
    }

    /**
     * Returns a number between the two given values {@code blendMin} and {@code blendMax}, based on factor.<br>
     * If {@code factor} = 0, returns {@code blendMin}. If {@code factor} = {@code range}, returns {@code blendMax}.<br>
     * @param blendMin The minimum value.
     * @param blendMax The maximum value.
     * @param factor The "progress" between blendMin and blendMax.
     * @param rangeMin The minimum of the range of values over which to interpolate.
     * @param rangeMax The maximum of the range of values over which to interpolate.
     * @return The interpolated value.
     */
    public static double blend(double blendMin, double blendMax, double factor, double rangeMin, double rangeMax)
    {
        if (factor <= rangeMin) return blendMin;
        if (factor >= rangeMax) return blendMax;
        return ((1 / (rangeMax - rangeMin)) * (factor - rangeMin)) * (blendMax - blendMin) + blendMin;
    }

    public static double getDistance(Entity entity, Vector3d pos)
    {
        double xDistance = Math.max(0, Math.abs(entity.getX() - pos.x) - entity.getBbWidth() / 2);
        double yDistance = Math.max(0, Math.abs((entity.getY() + entity.getBbHeight() / 2) - pos.y) - entity.getBbHeight() / 2);
        double zDistance = Math.max(0, Math.abs(entity.getZ() - pos.z) - entity.getBbWidth() / 2);
        return Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }

    public static double average(double... values)
    {
        double sum = 0;
        for (double value : values)
        {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * Takes an average of the two values, with weight<br>
     * The weight of an item should NEVER be 0.<br>
     * @param val1 The first value.
     * @param val2 The second value.
     * @param weight1 The weight of the first value.
     * @param weight2 The weight of the second value.
     * @return The weighted average.
     */
    public static double weightedAverage(double val1, double val2, double weight1, double weight2)
    {
        return (val1 * weight1 + val2 * weight2) / (weight1 + weight2);
    }

    /**
     * Takes an average of all the values in the given map, with weight<br>
     * The weight of an item should NEVER be 0.<br>
     * <br>
     * @param values The map of values to average (value, weight).
     * @return The average of the values in the given array.
     */
    public static double weightedAverage(Map<Double, Double> values)
    {
        double sum = 0;
        double weightSum = 0;
        for (Map.Entry<Double, Double> entry : values.entrySet())
        {
            sum += entry.getKey() * entry.getValue();
            weightSum += entry.getValue();
        }
        return sum / weightSum;
    }

    public static Vec3 vectorToVec(Vector3d vec)
    {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public static Direction getDirectionFromVector(double x, double y, double z)
    {
        Direction direction = Direction.NORTH;
        double f = Float.MIN_VALUE;

        for(Direction direction1 : Direction.values())
        {
            double f1 = x * direction1.getStepX() + y * direction1.getStepY() + z * direction1.getStepZ();

            if (f1 > f)
            {
                f = f1;
                direction = direction1;
            }
        }

        return direction;
    }

    public static <T> void breakableForEach(Collection<T> collection, BiConsumer<T, InterruptableStreamer<T>> consumer)
    {
        new InterruptableStreamer<T>(collection).run(consumer);
    }

    /**
     * A deobfuscated version of GuiComponent's innerBlit function.
     */
    public static void blit(Matrix4f matrix, int leftX, int rightX, int bottomY, int topY, int level, float leftU, float rightU, float topV, float bottomV)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, (float) leftX,  (float) topY,    (float) level).uv(leftU,  bottomV).endVertex();
        bufferbuilder.vertex(matrix, (float) rightX, (float) topY,    (float) level).uv(rightU, bottomV).endVertex();
        bufferbuilder.vertex(matrix, (float) rightX, (float) bottomY, (float) level).uv(rightU, topV).endVertex();
        bufferbuilder.vertex(matrix, (float) leftX,  (float) bottomY, (float) level).uv(leftU,  topV).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }

    public static void tryCatch(Runnable runnable)
    {
        try
        {
            runnable.run();
        }
        catch (Throwable throwable) {}
    }

    public static int getSign(Number value)
    {
        if (value.doubleValue() == 0) return 0;
        return (int) (value.doubleValue() / Math.abs(value.doubleValue()));
    }

    public static double crop(double value, int sigFigs)
    {
        return (int) (value * Math.pow(10.0, sigFigs)) / Math.pow(10.0, sigFigs);
    }

    public static int blendColors(int color1, int color2, float ratio)
    {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return r << 16 | g << 8 | b;
    }

    /**
     * @return The value that is farther from 0.
     */
    public static double getMostExtreme(double value1, double value2)
    {
        return Math.abs(value1) > Math.abs(value2) ? value1 : value2;
    }

    /**
     * @return The value that is closer to 0.
     */
    public static double getLeastExtreme(double value1, double value2)
    {
        return Math.abs(value1) < Math.abs(value2) ? value1 : value2;
    }
}
