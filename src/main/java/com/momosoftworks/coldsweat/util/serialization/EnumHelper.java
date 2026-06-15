package com.momosoftworks.coldsweat.util.serialization;

/**
 * Helper for resolving {@link StringRepresentable} enum constants by their serialized name.
 */
public class EnumHelper
{
    private EnumHelper() {}

    public static <T extends StringRepresentable> T byName(T[] values, String name)
    {
        if (name == null) return null;
        for (T value : values)
        {
            if (value.getSerializedName().equalsIgnoreCase(name))
            {   return value;
            }
        }
        return null;
    }
}
