package com.momosoftworks.coldsweat.api.util.placement;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;

import java.util.function.BiPredicate;

public enum Matcher implements StringRepresentable
{
    IGNORE("ignore", (modA, modB) -> false),
    SAME_CLASS("same_class", (modA, modB) -> modA.getClass().equals(modB.getClass())),
    SUBCLASS("subclass", (modA, modB) -> modA.getClass().isInstance(modB)),
    EQUALS("equals", TempModifier::equals);

    private final BiPredicate<TempModifier, TempModifier> predicate;
    private final String name;

    Matcher(String name, BiPredicate<TempModifier, TempModifier> predicate)
    {
        this.name = name;
        this.predicate = predicate;
    }

    public boolean check(TempModifier modA, TempModifier modB)
    {
        return predicate.test(modA, modB);
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }

    public static Matcher byName(String name)
    {
        return EnumHelper.byName(values(), name);
    }
}
