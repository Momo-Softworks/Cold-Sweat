package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.List;

public class InsulationTempModifier extends TempModifier
{
    public InsulationTempModifier() {}

    public InsulationTempModifier(int amount) {
        addArgument("amount", amount);
    }

    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        return temp.get() / Math.max(1d, (int) getArgument("amount") / 15d);
    }

    public String getID()
    {
        return "cold_sweat:insulated_armor";
    }
}