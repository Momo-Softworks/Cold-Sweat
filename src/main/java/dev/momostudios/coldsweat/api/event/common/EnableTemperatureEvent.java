package dev.momostudios.coldsweat.api.event.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.Set;

@Cancelable
public class EnableTemperatureEvent extends Event
{
    public static Set<EntityType<?>> ENABLED_ENTITIES = new HashSet<>();
    final LivingEntity entity;
    boolean enabled;

    public EnableTemperatureEvent(LivingEntity entity)
    {
        this.entity = entity;
    }

    public LivingEntity getEntity()
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
