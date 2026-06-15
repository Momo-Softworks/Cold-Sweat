package com.momosoftworks.coldsweat.api.util.placement;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;

import java.util.function.Predicate;

/**
 * Describes how a {@link TempModifier} should be inserted into an entity's modifier list.<br>
 * Ported from 1.16's {@code api/util/placement/Placement}.
 */
public class Placement
{
    private final Mode mode;
    private final Order order;
    private final Predicate<TempModifier> predicate;
    private Matcher matcher = Matcher.IGNORE;
    private int maxDuplicates = Integer.MAX_VALUE;
    private Placement fallback = null;

    public static final Placement FIRST = Placement.of(Mode.ADD_BEFORE, Order.FIRST, mod -> true);
    public static final Placement LAST = Placement.of(Mode.ADD_AFTER, Order.LAST, mod -> true);

    Placement(Mode mode, Order order, Predicate<TempModifier> predicate)
    {   this.mode = mode;
        this.order = order;
        this.predicate = predicate;
    }

    public static Placement of(Mode mode, Order order, Predicate<TempModifier> predicate)
    {   return new Placement(mode, order, predicate);
    }

    public Placement limitDuplicates(Matcher duplicateMatcher, int maxDuplicates)
    {   this.matcher = duplicateMatcher;
        this.maxDuplicates = maxDuplicates;
        return this;
    }

    public Placement noDuplicates(Matcher duplicateMatcher)
    {   this.matcher = duplicateMatcher;
        this.maxDuplicates = 1;
        return this;
    }

    public Mode mode()
    {   return mode;
    }
    public Order order()
    {   return order;
    }
    public Predicate<TempModifier> predicate()
    {   return predicate;
    }
    public Placement fallback()
    {   return fallback;
    }
    public Matcher duplicates()
    {   return matcher;
    }
    public int maxDuplicates()
    {   return maxDuplicates;
    }

    public Placement orElse(Placement fallback)
    {   this.fallback = fallback;
        return this;
    }
}
