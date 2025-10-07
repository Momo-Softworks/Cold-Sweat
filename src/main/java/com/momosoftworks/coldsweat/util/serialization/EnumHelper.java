package com.momosoftworks.coldsweat.util.serialization;

public class EnumHelper
{
    public static <T extends Enum<T> & StringRepresentable> T byName(T[] values, String name)
    {
        if (values.length == 0)
        {   throw new IllegalArgumentException("Tried to get enum value from empty enum");
        }
        for (T value : values)
        {
            if (value.getSerializedName().equalsIgnoreCase(name))
            {   return value;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for enum %s: %s", values[0].getClass().getSimpleName(), name));
    }
}
