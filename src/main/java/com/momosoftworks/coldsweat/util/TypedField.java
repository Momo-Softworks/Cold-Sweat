package com.momosoftworks.coldsweat.util;

import java.lang.reflect.Field;

public class TypedField<T>
{
    private final Field field;

    public TypedField(Field field)
    {   this.field = field;
    }

    public Field field()
    {   return field;
    }

    public static <F> TypedField<F> of(Field field)
    {   return new TypedField<>(field);
    }

    @SuppressWarnings("unchecked")
    public T get(Object obj)
    {   try
        {   return (T) field.get(obj);
        }
        catch (IllegalAccessException e)
        {   throw new RuntimeException(e);
        }
    }
}
