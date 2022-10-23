package dev.momostudios.coldsweat.api.temperature.modifier;

import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

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
    public Function<Temperature, Temperature> calculate(Player player)
    {
        double worldTemp = TempHelper.getTemperature(player, Temperature.Type.WORLD).get();
        double maxTemp = ConfigSettings.getInstance().maxTemp;
        double minTemp = ConfigSettings.getInstance().minTemp;

        double strength = this.<Double>getArgument("strength");
        double returnRate = Math.min(-0.001, -0.001 - (worldTemp / 800));
        double addAmount = player.isInWaterOrBubble() ? 0.01 : player.level.isRainingAt(player.blockPosition()) ? 0.005 : returnRate;
        double maxStrength = CSMath.clamp(Math.abs(CSMath.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);

        setArgument("strength", CSMath.clamp(strength + addAmount, 0d, maxStrength));

        // If the strength is 0, this TempModifier expires
        if (strength <= 0.0)
        {
            this.expires(this.getTicksExisted() - 1);
        }

        if (!player.isInWater())
        {
            if (Math.random() < Math.min(0.5, strength))
            {
                double randX = player.getBbWidth() * (Math.random() - 0.5);
                double randY = (player.getEyeHeight() * 0.8) * Math.random();
                double randZ = player.getBbWidth() * (Math.random() - 0.5);
                player.level.addParticle(ParticleTypes.FALLING_WATER, player.getX() + randX, player.getY() + randY, player.getZ() + randZ, 0, 0, 0);
            }
        }

        return temp -> temp.add(-this.<Double>getArgument("strength"));
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
