package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

@Cancelable
public class EnableTemperatureEvent extends Event implements IModBusEvent
{
    final EntityType<?> entityType;
    boolean enabled = false;

    public EnableTemperatureEvent(EntityType<?> entityType)
    {   this.entityType = entityType;
    }

    public EntityType<?> getEntityType()
    {   return entityType;
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
