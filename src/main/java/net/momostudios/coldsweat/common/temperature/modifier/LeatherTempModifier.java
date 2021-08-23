package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.Temperature;

import java.util.List;

public class LeatherTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    public double amount = 0;
    public LeatherTempModifier() {}

    public LeatherTempModifier(List<INBT> args)
    {
        this.amount = ((NumberNBT) args.get(0)).getDouble();
    }


    public LeatherTempModifier with(List<INBT> args)
    {
        return new LeatherTempModifier(args);
    }

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifier(player, LeatherTempModifier.class, PlayerTemp.Types.RATE, 2);
        return temp.get() / Math.max(1, amount / 15);
    }
}