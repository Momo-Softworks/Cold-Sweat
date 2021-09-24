package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.WorldInfo;

import java.util.Arrays;
import java.util.List;

public class HearthTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    WorldTemperatureConfig config = WorldTemperatureConfig.getInstance();

    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        //if (player.ticksExisted % 5 == 0) PlayerTemp.removeModifier(player, HearthTempModifier.class, PlayerTemp.Types.AMBIENT, 1);

        ColdSweatConfig config = ColdSweatConfig.getInstance();
        return (config.maxHabitable() + config.minHabitable()) / 2;
    }

    public String getID()
    {
        return "cold_sweat:hearth_insulation";
    }
}