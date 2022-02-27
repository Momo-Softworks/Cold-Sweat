package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier() {}

    public InsulationTempModifier(int amount) {
        addArgument("amount", amount);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        return temp.get() / Math.max(1d, (int) getArgument("amount") / 10d);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}