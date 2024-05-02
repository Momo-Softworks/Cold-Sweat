package com.momosoftworks.coldsweat.util.serialization;

import java.util.function.Supplier;

public class ObjectBuilder
{
    public static <T> T build(Supplier<T> object)
    {   return object.get();
    }

    public static String formatComplexString(String string)
    {
        int indentation = 0;
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray())
        {
            if (c == '{' || c == '[')
            {
                builder.append(c).append('\n');
                indentation++;
                builder.append("    ".repeat(indentation));
            }
            else if (c == '}' || c == ']')
            {
                builder.append('\n');
                indentation--;
                builder.append("    ".repeat(indentation));
                builder.append(c);
            }
            else if (c == ',')
            {
                builder.append(c).append('\n');
                builder.append("    ".repeat(indentation));
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}
