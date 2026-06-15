package com.momosoftworks.coldsweat.api.temperature.modifier;

/**
 * The cooling half of a thermal source (Hearth/Icebox). Ported from 1.16. Id {@code cold_sweat:cooling}.
 */
public class FrigidnessTempModifier extends ThermalSourceTempModifier
{
    public FrigidnessTempModifier()
    {   this(0);
    }

    public FrigidnessTempModifier(int strength)
    {   super(strength, 0);
    }

    @Override
    public int getStrength()
    {   return this.getCooling();
    }

    public String getID()
    {   return "cold_sweat:cooling";
    }
}
