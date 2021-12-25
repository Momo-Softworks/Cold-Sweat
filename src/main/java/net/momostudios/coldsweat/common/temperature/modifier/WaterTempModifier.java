package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.PlayerTemp;
import net.momostudios.coldsweat.core.util.Units;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {
        addArgument("strength", 1d);
    }

    public WaterTempModifier(double strength)
    {
        addArgument("strength", strength);
    }

    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        try
        {
            double strength = (double) getArgument("strength");
            double factor = Math.min(-0.01, -0.01 - temp.get() / 50);
            setArgument("strength", MathHelperCS.clamp(strength + (player.isInWater() ? 0.3 : factor), 0, 10));

            if (!player.isInWater() && strength > 0.0)
            {
                if (Math.random() < strength / 40.0)
                {
                    double randX = player.getWidth() * (Math.random() - 0.5);
                    double randY = player.getHeight() * Math.random();
                    double randZ = player.getWidth() * (Math.random() - 0.5);
                    player.world.addParticle(ParticleTypes.FALLING_WATER, player.getPosX() + randX, player.getPosY() + randY, player.getPosZ() + randZ, 0, 0, 0);
                }
            }
            double ambientEffect = Math.min(0, temp.get() - ConfigCache.getInstance().minTemp);

            return temp.get() + MathHelperCS.convertUnits(-strength, Units.F, Units.MC, false) + ambientEffect * (strength / 40);
        }
        // Remove the modifier if an exception is thrown
        catch (Exception e)
        {
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
