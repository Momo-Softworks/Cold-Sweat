package com.momosoftworks.coldsweat.compat.kubejs.event;

import com.momosoftworks.coldsweat.api.event.common.temperautre.TemperatureChangedEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import dev.latvian.mods.kubejs.entity.EntityJS;
import dev.latvian.mods.kubejs.entity.LivingEntityEventJS;
import dev.latvian.mods.kubejs.entity.LivingEntityJS;

public class TempChangedEventJS extends LivingEntityEventJS
{
    private final TemperatureChangedEvent event;

    public TempChangedEventJS(TemperatureChangedEvent event)
    {   this.event = event;
    }

    @Override
    public EntityJS getEntity()
    {   return new LivingEntityJS(event.getEntity());
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
