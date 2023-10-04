package com.momosoftworks.coldsweat.api.event.common;


import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLivingBase;

@Cancelable
public class EnableTemperatureEvent extends Event
{
    final EntityLivingBase entity;
    boolean enabled;

    public EnableTemperatureEvent(EntityLivingBase entity)
    {
        this.entity = entity;
    }

    public EntityLivingBase getEntity()
    {
        return entity;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
