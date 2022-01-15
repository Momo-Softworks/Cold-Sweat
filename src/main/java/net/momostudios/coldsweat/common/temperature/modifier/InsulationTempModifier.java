package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier() {}

    public InsulationTempModifier(int amount) {
        addArgument("amount", amount);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        return temp.get() / Math.max(1d, (int) getArgument("amount") / 10d);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}