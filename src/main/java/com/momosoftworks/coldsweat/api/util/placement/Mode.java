package com.momosoftworks.coldsweat.api.util.placement;

import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;

public enum Mode implements StringRepresentable
{
    // Inserts the new modifier before the targeted modifier's position
    ADD_BEFORE("add_before"),
    // Inserts the new modifier after the targeted modifier's position
    ADD_AFTER("add_after"),
    // Replace the desired instance of the modifier (fails if no modifiers pass the predicate)
    REPLACE("replace");

    private final String name;

    Mode(String name)
    {   this.name = name;
    }

    public boolean isAdding()
    {   return this == ADD_BEFORE || this == ADD_AFTER;
    }

    @Override
    public String getSerializedName()
    {   return name;
    }

    public static Mode byName(String name)
    {   return EnumHelper.byName(values(), name);
    }
}
