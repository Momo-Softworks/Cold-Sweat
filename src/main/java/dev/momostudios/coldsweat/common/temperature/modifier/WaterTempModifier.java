package dev.momostudios.coldsweat.common.temperature.modifier;

import dev.momostudios.coldsweat.util.CSMath;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import dev.momostudios.coldsweat.config.ConfigCache;

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
    public double getResult(Temperature temp, PlayerEntity player)
    {
        double maxTemp = ConfigCache.getInstance().maxTemp;
        double minTemp = ConfigCache.getInstance().minTemp;

        try
        {
            double strength = getArgument("strength", Double.class);
            double returnRate = Math.min(-0.0003, -0.0003 - (temp.get() / 800));
            double addAmount = player.isInWaterOrBubbleColumn() ? 0.01 : player.world.isRainingAt(player.getPosition()) ? 0.005 : returnRate;

            setArgument("strength", CSMath.clamp(strength + addAmount, 0, Math.abs(CSMath.average(maxTemp, minTemp) - temp.get()) / 2));

            if (!player.isInWater() && strength > 0.0)
            {
                if (Math.random() < strength)
                {
                    double randX = player.getWidth() * (Math.random() - 0.5);
                    double randY = player.getHeight() * Math.random();
                    double randZ = player.getWidth() * (Math.random() - 0.5);
                    player.world.addParticle(ParticleTypes.FALLING_WATER, player.getPosX() + randX, player.getPosY() + randY, player.getPosZ() + randZ, 0, 0, 0);
                }
            }

            return temp.get() - getArgument("strength", Double.class);
        }
        // Remove the modifier if an exception is thrown
        catch (Exception e)
        {
            e.printStackTrace();
            args.remove("strength");
            args.put("strength", 1d);
            return temp.get();
        }
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
