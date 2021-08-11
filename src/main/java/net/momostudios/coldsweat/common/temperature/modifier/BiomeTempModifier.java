package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.core.util.WorldInfo;
import net.momostudios.coldsweat.common.temperature.Temperature;

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