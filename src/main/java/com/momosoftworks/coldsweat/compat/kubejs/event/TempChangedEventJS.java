package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.api.event.common.temperautre.TemperatureChangedEvent;
import dev.latvian.mods.kubejs.entity.KubeLivingEntityEvent;
import net.minecraft.world.entity.LivingEntity;

public class TempChangedEventJS implements KubeLivingEntityEvent
{
    private final TemperatureChangedEvent event;

    public TempChangedEventJS(TemperatureChangedEvent event)
    {   this.event = event;
    }

    @Override
    public LivingEntity getEntity()
    {   return event.getEntity();
    }

    public String getTrait()
    {   return event.getTrait().getSerializedName();
    }

    public double getOldTemperature()
    {   return event.getOldTemperature();
    }

    public double getTemperature()
    {   return event.getTemperature();
    }

    public void setTemperature(double newTemperature)
    {   event.setTemperature(newTemperature);
    }
}
