package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier() {}

    public WaterskinTempModifier(double temp)
    {
        addArgument("temperature", temp);
    }

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifier(player, WaterskinTempModifier.class, PlayerTemp.Types.BODY, 2);
        return temp.get() + (double) getArgument("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}