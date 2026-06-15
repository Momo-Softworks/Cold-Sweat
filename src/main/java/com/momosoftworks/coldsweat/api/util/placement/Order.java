package com.momosoftworks.coldsweat.api.util.placement;

import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;

public enum Order implements StringRepresentable
{
    // Targets the first modifier that passes the predicate
    FIRST("first"),
    // Targets the last modifier that passes the predicate
    LAST("last");

    private final String name;

    Order(String name)
    {   this.name = name;
    }

    @Override
    public String getSerializedName()
    {   return name;
    }

    public static Order byName(String name)
    {   return EnumHelper.byName(values(), name);
    }
}
