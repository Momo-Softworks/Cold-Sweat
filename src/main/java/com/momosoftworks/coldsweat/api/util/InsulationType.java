package com.momosoftworks.coldsweat.api.util;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.serialization.StringRepresentable;

public enum InsulationType implements StringRepresentable
{
    ITEM("item"),
    ADAPTIVE("adaptive"),
    CURIO("curio"),
    ARMOR("armor");

    final String name;

    InsulationType(String name)
    {   this.name = name;
    }

    public static final Codec<InsulationType> CODEC = Codec.STRING.xmap(InsulationType::byName, InsulationType::getSerializedName);

    @Override
    public String getSerializedName()
    {   return name;
    }

    public static InsulationType byName(String name)
    {   for (InsulationType type : values())
        {   if (type.name.equals(name))
            {   return type;
            }
        }
        throw new IllegalArgumentException("Unknown insulation type: " + name);
    }
}
