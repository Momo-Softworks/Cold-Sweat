package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.PlayerTemp;
import net.momostudios.coldsweat.common.temperature.Temperature;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class WaterskinTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    int amount = 0;

    @Override
    public WaterskinTempModifier with(List<Object> args)
    {
        this.amount = (int) args.get(0);
        return new WaterskinTempModifier();
    }

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        PlayerTemp.removeModifier(player, WaterskinTempModifier.class, PlayerTemp.Types.BODY, Integer.MAX_VALUE);
        return temp.get() + amount;
    }
}