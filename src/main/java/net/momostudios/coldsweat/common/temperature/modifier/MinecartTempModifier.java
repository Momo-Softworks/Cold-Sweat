package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.util.PlayerTemp;

public class MinecartTempModifier extends TempModifier
{
    public MinecartTempModifier() {}

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifiers(player, PlayerTemp.Types.RATE, 1, modifier -> modifier.getID().equals(getID()));

        return 0;
    }

    public String getID()
    {
        return "cold_sweat:insulated_minecart";
    }
}