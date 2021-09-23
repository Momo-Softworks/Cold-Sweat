package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.WorldInfo;

public class DepthTempModifier extends TempModifier implements IForgeRegistryEntry<TempModifier>
{
    @Override
    public double calculate(Temperature temp, PlayerEntity player)
    {
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
            int level = WorldInfo.getGroundLevel(iterator, player.world);
            // Arbitrary calculation that makes insulation less effective if the BlockPos is exposed to air
            if (y > level)
                surfaceNumber++;

            // Add the height to the total
            weightedHeight += Math.max(0, level - y);
        }
        // Get the average depth from the samples
        weightedHeight /= 100;

        // Gets the depth
        double divisorSurface = Math.max(0, (weightedHeight * 3) / surfaceNumber);

        return midTemp.get() + ((temp.get() - midTemp.get()) / (1 + divisorSurface));
    }

    public String getID()
    {
        return "cold_sweat:depth";
    }
}
