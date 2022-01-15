package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.util.MathHelperCS;
import net.momostudios.coldsweat.util.WorldInfo;

public class DepthTempModifier extends TempModifier
{
    @Override
    public double getResult(Temperature temp, PlayerEntity player)
    {
        if (player.world.getDimensionType().getHasCeiling())
            return temp.get();

        double y = player.getPosY();
        ConfigCache config = ConfigCache.getInstance();
        Temperature midTemp = new Temperature((config.maxTemp + config.minTemp) / 2d);

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
        double factor = Math.max(0, weightedHeight - surfaceNumber);
        double level = MathHelperCS.clamp((-1/30d) * factor + 1, 0, 1);
        return midTemp.get() + (temp.get() - midTemp.get()) * level;
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
