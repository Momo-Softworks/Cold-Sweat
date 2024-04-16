package com.momosoftworks.coldsweat.api.util;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;

import java.util.function.Predicate;

public record Placement(Mode mode, Order order, Predicate<TempModifier> predicate)
{
    public static final Placement AFTER_LAST = Placement.of(Mode.AFTER, Order.LAST, mod -> true);
    public static final Placement BEFORE_FIRST = Placement.of(Mode.BEFORE, Order.FIRST, mod -> true);

    public static Placement of(Mode mode, Order order, Predicate<TempModifier> predicate)
    {
        return new Placement(mode, order, predicate);
    }

    public enum Mode
    {
        // Inserts the new modifier before the targeted modifier's position
        BEFORE,
        // Inserts the new modifier after the targeted modifier's position
        AFTER,
        // Replace the desired instance of the modifier (fails if no modifiers pass the predicate)
        REPLACE,
        // Replace the desired instance of the modifier if it exists, otherwise add it to the end
        REPLACE_OR_ADD
    }

    public enum Order
    {
        // Targets the first modifier that passes the predicate
        FIRST,
        // Targets the last modifier that passes the predicate
        LAST
    }
}
