package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.Units;

import java.util.ArrayList;
import java.util.List;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier() {}

    public WaterTempModifier(double strength)
    {
        addArgument("strength", strength);
    }

    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        setArgument("strength", MathHelperCS.clamp((double) getArgument("strength") + (player.isInWater() ? 0.5 : -0.04), 0, 10));

        return temp.get() + MathHelperCS.convertUnits(-(double) getArgument("strength"), Units.F, Units.MC, false);
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
