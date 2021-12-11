package net.momostudios.coldsweat.common.temperature.modifier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.momostudios.coldsweat.common.temperature.Temperature;
import net.momostudios.coldsweat.core.util.MathHelperCS;
import net.momostudios.coldsweat.core.util.Units;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {
        addArgument("strength", 1);
    }

    public WaterTempModifier(double strength)
    {
        addArgument("strength", strength);
    }

    @Override
    public double getValue(Temperature temp, PlayerEntity player)
    {
        double strength = (double) getArgument("strength");
        setArgument("strength", MathHelperCS.clamp(strength + (player.isInWater() ? 0.3 : -0.02), 0, 10));

        if (!player.isInWater() && strength > 0)
        {
            if (Math.random() < strength / 15)
            {
                double randX = player.getWidth() * (Math.random() - 0.5);
                double randY = player.getHeight() * Math.random();
                double randZ = player.getWidth() * (Math.random() - 0.5);
                player.world.addParticle(ParticleTypes.FALLING_WATER, player.getPosX() + randX, player.getPosY() + randY, player.getPosZ() + randZ, 0, 0, 0);
            }
        }

        return temp.get() + MathHelperCS.convertUnits(-strength, Units.F, Units.MC, false);
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
