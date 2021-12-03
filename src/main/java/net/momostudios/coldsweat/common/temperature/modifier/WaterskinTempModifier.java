package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
    public double getValue(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifiers(player, PlayerTemp.Types.BODY, 1, modifier -> modifier.equals(this));
        return temp.get() + (double) getArgument("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}