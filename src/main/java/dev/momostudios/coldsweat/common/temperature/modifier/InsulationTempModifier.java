package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.world.entity.player.Player;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier() {
        addArgument("warmth", 0);
    }

    public InsulationTempModifier(int amount) {
        addArgument("warmth", amount);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {   
        return temp.get() / Math.max(1d, this.getArgument("warmth", int.class) / 10d);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}