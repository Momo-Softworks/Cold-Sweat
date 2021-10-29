package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;

public class DepthTempModifier extends TempModifier
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
        if (!player.world.getDimensionType().getHasCeiling())
        {
            //System.out.println(player.world.getDimensionKey().getRegistryName());
            double y = player.getPosY();
            ColdSweatConfig config = ColdSweatConfig.getInstance();
            Temperature midTemp = new Temperature((config.maxHabitable() + config.minHabitable()) / 2);

            // The total height across all samples
            double weightedHeight = 0;
            //
            double surfaceNumber = 0.01;

            for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), 100, 4))
            {
                // Get the surface height at the BlockPos
                int level = WorldInfo.getGroundLevel(iterator, player.world) - 2;
                // Makes insulation less effective if the BlockPos is exposed to air
                if (y > level)
                    surfaceNumber++;

                // Add the height to the total
                weightedHeight += Math.max(0, level - y);
            }
            // Get the average depth from the samples
            weightedHeight /= 100;

            // Gets the depth
            double divisorSurface = Math.max(0, (weightedHeight * 0.5) / surfaceNumber);

            return midTemp.get() + (temp.get() - midTemp.get()) / (1 + divisorSurface);
        }
        else
            return temp.get();
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
