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

        // Samples the average height using 100 samples of nearby blocks
        double weightedHeight = 0;
        double surfaceNumber = 0.1;
        for (BlockPos iterator : WorldInfo.getNearbyPositions(player.getPosition(), 100, 8))
        {
            int level = WorldInfo.getGroundLevel(iterator, player.world);
            if (y > level)
                surfaceNumber *= 1.5;

            weightedHeight += level;
        }
        weightedHeight /= 100;

        double averageDepth = Math.max(0, weightedHeight - y);

        double divisor = (averageDepth / 25 + 1);
        double divisorSurface = Math.max(1, divisor - surfaceNumber);
        divisorSurface = Math.max(1, (divisor + divisorSurface) / 2);
        return midTemp.get() + ((temp.get() - midTemp.get()) / divisorSurface);
    }
}
