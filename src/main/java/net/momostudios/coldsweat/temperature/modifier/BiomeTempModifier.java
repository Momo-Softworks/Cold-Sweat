package net.momostudios.coldsweat.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.momostudios.coldsweat.util.world.WorldInfo;
import net.momostudios.coldsweat.temperature.Temperature;

import java.util.List;

public class BiomeTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        double worldTemp = 0;
        for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), player.world, 200, 6))
        {
            worldTemp += player.world.getBiome(iterator).getTemperature(iterator);
        }

        return temp.get() + (worldTemp / 200);
    }
}