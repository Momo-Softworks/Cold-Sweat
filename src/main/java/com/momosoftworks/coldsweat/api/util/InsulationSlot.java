package com.momosoftworks.coldsweat.api.util;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;

public enum InsulationSlot implements StringRepresentable
{
    ITEM("item"),
    CURIO("curio"),
    ARMOR("armor");

    final String name;

    InsulationSlot(String name)
    {   this.name = name;
    }

    public static final Codec<InsulationSlot> CODEC = Codec.STRING.xmap(InsulationSlot::byName, InsulationSlot::getSerializedName);

    @Override
    public String getSerializedName()
    {   return name;
    }

    public static InsulationSlot byName(String name)
    {   for (InsulationSlot type : values())
        {   if (type.name.equals(name))
            {   return type;
            }
        }
        throw new IllegalArgumentException("Unknown insulation type: " + name);
    }
}
