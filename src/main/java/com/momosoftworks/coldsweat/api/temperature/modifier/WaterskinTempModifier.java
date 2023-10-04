package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Function;

public class WaterskinTempModifier extends TempModifier
{
    public WaterskinTempModifier()
    {
        this(0.0);
    }

    public WaterskinTempModifier(double temp)
    {
        this.getNBT().setDouble("temperature", temp);
    }

    @Override
    public Function<Double, Double>  calculate(EntityLivingBase entity, Temperature.Type type)
    {   return temp -> temp + this.getNBT().getDouble("temperature");
    }

    public String getID()
    {
        return "cold_sweat:waterskin";
    }
}