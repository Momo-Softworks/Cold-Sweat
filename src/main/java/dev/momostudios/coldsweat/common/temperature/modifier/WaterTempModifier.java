package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {
        addArgument("strength", 0.01);
    }

    public WaterTempModifier(double strength)
    {
        addArgument("strength", strength);
    }

    @Override
    public double getResult(Temperature temp, Player player)
    {
        double maxTemp = ConfigCache.getInstance().maxTemp;
        double minTemp = ConfigCache.getInstance().minTemp;

        try
        {
            double strength = getArgument("strength", Double.class);
            double returnRate = Math.min(-0.0003, -0.0003 - (temp.get() / 800));
            double addAmount = player.isInWaterOrBubble() ? 0.01 : player.level.isRainingAt(player.blockPosition()) ? 0.005 : returnRate;

            setArgument("strength", CSMath.clamp(strength + addAmount, 0d, Math.abs(CSMath.average(maxTemp, minTemp) - temp.get()) / 2));

            if (!player.isInWater() && strength > 0.0)
            {
                if (Math.random() < strength)
                {
                    double randX = player.getBbWidth() * (Math.random() - 0.5);
                    double randY = player.getBbHeight() * Math.random();
                    double randZ = player.getBbWidth() * (Math.random() - 0.5);
                    player.level.addParticle(ParticleTypes.FALLING_WATER, player.getX() + randX, player.getY() + randY, player.getZ() + randZ, 0, 0, 0);
                }
            }

            return temp.get() - getArgument("strength", Double.class);
        }
        // Remove the modifier if an exception is thrown
        catch (Exception e)
        {
            e.printStackTrace();
            clearArgument("strength");
            setArgument("strength", 1d);
            return temp.get();
        }
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
