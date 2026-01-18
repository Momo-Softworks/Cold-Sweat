package com.momosoftworks.coldsweat.util.render;

/**
 * Duplicate of {@link net.minecraft.util.ColorHelper} because they made some of the methods client-only for some reason.
 */
public class PackedColorHelper
{
    public static int alpha(int packedColor)
    {   return packedColor >>> 24;
    }
    public static int red(int packedColor)
    {   return packedColor >> 16 & 255;
    }
    public static int green(int packedColor)
    {   return packedColor >> 8 & 255;
    }
    public static int blue(int packedColor)
    {   return packedColor & 255;
    }

    /**
     * Compile a color from ARGB values (0 - 255)
     * @return The packed color
     */
    public static int color(int alpha, int red, int green, int blue)
    {   return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * Multiplies two packed colors together.
     * @return The multiplied color
     */
    public static int multiply(int colorA, int colorB)
    {   return color(alpha(colorA) * alpha(colorB) / 255, red(colorA) * red(colorB) / 255, green(colorA) * green(colorB) / 255, blue(colorA) * blue(colorB) / 255);
    }

    /**
     * Mixes two packed colors together with the given ratio.
     * @param ratio 0.0 = all colorA, 1.0 = all colorB, 0.5 = even mix
     * @return The mixed color
     */
    public static int mix(int colorA, int colorB, float ratio)
    {
        int aFrom = (colorA >> 24) & 0xff;
        int rFrom = (colorA >> 16) & 0xff;
        int gFrom = (colorA >> 8) & 0xff;
        int bFrom = colorA & 0xff;

        int aTo = (colorB >> 24) & 0xff;
        int rTo = (colorB >> 16) & 0xff;
        int gTo = (colorB >> 8) & 0xff;
        int bTo = colorB & 0xff;

        int a = (int) (aFrom + (aTo - aFrom) * ratio);
        int r = (int) (rFrom + (rTo - rFrom) * ratio);
        int g = (int) (gFrom + (gTo - gFrom) * ratio);
        int b = (int) (bFrom + (bTo - bFrom) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
