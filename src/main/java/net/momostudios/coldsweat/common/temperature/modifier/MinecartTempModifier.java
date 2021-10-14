package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

public class MinecartTempModifier extends TempModifier
{
    public MinecartTempModifier() {}

    public MinecartTempModifier(List<INBT> args)
    {
    }

    public MinecartTempModifier with(List<INBT> args)
    {
        return new MinecartTempModifier(args);
    }

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifier(player, MinecartTempModifier.class, PlayerTemp.Types.RATE, 1);

        return 0;
    }

    public String getID()
    {
        return "cold_sweat:insulated_minecart";
    }
}