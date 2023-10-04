package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.client.particle.EntitySpellParticleFX;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public class WaterTempModifier extends TempModifier
{
    public WaterTempModifier()
    {   this(0.01);
    }

    public WaterTempModifier(double strength)
    {   this.getNBT().setDouble("Strength", strength);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        double worldTemp = Temperature.get(entity, Temperature.Type.WORLD);
        double maxTemp = ConfigSettings.MAX_TEMP.get();
        double minTemp = ConfigSettings.MIN_TEMP.get();

        double strength = this.getNBT().getDouble("Strength");
        double returnRate = Math.min(-0.0012, -0.0012 - (worldTemp / 640));
        double addAmount = WorldHelper.isInWater(entity) ? 0.05 : WorldHelper.isRainingAt(entity.worldObj, new BlockPos(entity)) ? 0.0125 : returnRate;
        double maxStrength = CSMath.clamp(Math.abs(CSMath.average(maxTemp, minTemp) - worldTemp) / 2, 0.23d, 0.5d);

        double newStrength = CSMath.clamp(strength + addAmount, 0d, maxStrength);
        this.getNBT().setDouble("Strength", newStrength);

        // If the strength is 0, this TempModifier expires
        if (strength <= 0.0)
        {   this.expires(this.getTicksExisted() - 1);
        }

        return temp ->
        {
            if (!entity.isInWater())
            {
                if (Math.random() < strength * 2)
                {   double randX = entity.width * (Math.random() - 0.5);
                    double randY = entity.height * Math.random();
                    double randZ = entity.width * (Math.random() - 0.5);
                    entity.worldObj.spawnParticle("splash", entity.posX + randX, entity.posY - entity.height + randY, entity.posZ + randZ, 0, 0, 0);
                }
            }
            return temp - newStrength;
        };
    }

    @Override
    public String getID()
    {
        return "cold_sweat:water";
    }
}
