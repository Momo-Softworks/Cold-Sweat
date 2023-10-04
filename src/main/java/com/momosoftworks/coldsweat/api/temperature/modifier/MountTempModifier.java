package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public class MountTempModifier extends TempModifier
{
    public MountTempModifier()
    {   this(0, 0);
    }

    public MountTempModifier(int warming, int cooling)
    {   this.getNBT().setDouble("Warming", warming);
        this.getNBT().setDouble("Cooling", cooling);
    }

    @Override
    public Function<Double, Double> calculate(EntityLivingBase entity, Temperature.Type type)
    {
        return temp -> temp > 0
                       ? temp / (1 + this.getNBT().getDouble("Cooling"))
                       : temp / (1 + this.getNBT().getDouble("Warming"));
    }

    public String getID()
    {
        return "cold_sweat:mount";
    }
}