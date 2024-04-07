package com.momosoftworks.coldsweat.util.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ListBuilder<T>
{
    ArrayList<T> elements = new ArrayList<>();

    @SafeVarargs
    private ListBuilder(T... elements)
    {   this.elements.addAll(Arrays.asList(elements));
    }

    private ListBuilder(T element)
    {   this.elements.add(element);
    }

    @SafeVarargs
    public static <E> ListBuilder<E> begin(E... elements)
    {   return new ListBuilder<>(elements);
    }

    public static <E> ListBuilder<E> begin(E element)
    {   return new ListBuilder<>(element);
    }

    /**
     * Adds an element to the list if the condition is true.
     * The objects are not created if the condition is false.
     * @param condition The condition to check.
     * @param elements The elements to add.
     * @return The ListBuilder instance.
     */
    @SafeVarargs
    public final ListBuilder<T> addIf(boolean condition, Supplier<T>... elements)
    {   if (condition) this.elements.addAll(Arrays.stream(elements).map(Supplier::get).collect(Collectors.toList()));
        return this;
    }

    /**
     * Adds an element to the list if the condition is true.
     * The object is not created if the condition is false.
     * @param condition The condition to check.
     * @param supplier The element to add.
     * @return The ListBuilder instance.
     */
    public final ListBuilder<T> addIf(boolean condition, Supplier<T> supplier)
    {
        if (condition)
        {   T element = supplier.get();
            if (element != null)
            {   this.elements.add(element);
            }
        }
        return this;
    }

    @SafeVarargs
    public final ListBuilder<T> add(T... elements)
    {   this.elements.addAll(Arrays.asList(elements));
        return this;
    }

    public ListBuilder<T> add(T element)
    {   this.elements.add(element);
        return this;
    }

    public ArrayList<T> build()
    {   return elements;
    }
}
