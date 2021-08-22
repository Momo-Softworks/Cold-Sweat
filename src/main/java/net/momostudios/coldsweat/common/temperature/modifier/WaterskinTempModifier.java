package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.Temperature;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaterskinTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    public double amount = 0;
    public WaterskinTempModifier() {}

    public WaterskinTempModifier(List<INBT> args)
    {
        this.amount = ((NumberNBT) args.get(0)).getDouble();
    }


    public WaterskinTempModifier with(List<INBT> args)
    {
        return new WaterskinTempModifier(args);
    }

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifier(player, WaterskinTempModifier.class, PlayerTemp.Types.BODY, 2);
        return temp.get() + amount;
    }
}