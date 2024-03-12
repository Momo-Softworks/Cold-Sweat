package com.momosoftworks.coldsweat.util.serialization;

import java.util.function.Supplier;

public class ObjectBuilder
{
    public static <T> T build(Supplier<T> object)
    {   return object.get();
    }
}
