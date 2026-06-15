package com.momosoftworks.coldsweat.api.event.common;

import com.momosoftworks.coldsweat.api.util.Temperature;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

/**
 * Fired when an entity's stored temperature value (of a given {@link Temperature.Type}) is about to change. <br>
 * <br>
 * {@link #entity} is the entity whose temperature is changing. <br>
 * {@link #type} is the {@link Temperature.Type} being changed. <br>
 * {@link #oldTemperature} is the value before the change. <br>
 * {@link #temperature} is the new value (mutable). <br>
 * <br>
 * This event is {@link Cancelable}. Canceling it prevents the value from changing. <br>
 * Fired on the {@link MinecraftForge#EVENT_BUS}.
 */
@Cancelable
public class TemperatureChangedEvent extends Event
{
    private final EntityLivingBase entity;
    private final Temperature.Type type;
    private final double oldTemperature;
    private double temperature;

    public TemperatureChangedEvent(EntityLivingBase entity, Temperature.Type type, double oldTemperature, double temperature)
    {   this.entity = entity;
        this.type = type;
        this.oldTemperature = oldTemperature;
        this.temperature = temperature;
    }

    public EntityLivingBase getEntity()
    {   return entity;
    }

    public Temperature.Type getType()
    {   return type;
    }

    public double getOldTemperature()
    {   return oldTemperature;
    }

    public double getTemperature()
    {   return temperature;
    }

    public void setTemperature(double temperature)
    {   this.temperature = temperature;
    }
}
