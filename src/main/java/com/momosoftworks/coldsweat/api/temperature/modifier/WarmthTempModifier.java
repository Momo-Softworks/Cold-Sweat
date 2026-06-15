package com.momosoftworks.coldsweat.api.temperature.modifier;

/**
 * The heating half of a thermal source (Hearth/Boiler). Ported from 1.16. Id {@code cold_sweat:warming}.
 */
public class WarmthTempModifier extends ThermalSourceTempModifier
{
    public WarmthTempModifier()
    {   this(0);
    }

    public WarmthTempModifier(int strength)
    {   super(0, strength);
    }

    @Override
    public int getStrength()
    {   return this.getWarming();
    }

    public String getID()
    {   return "cold_sweat:warming";
    }
}
