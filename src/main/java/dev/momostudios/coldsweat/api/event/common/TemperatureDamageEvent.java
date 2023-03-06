package dev.momostudios.coldsweat.api.event.common;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fired before temperature damage is applied to an entity.
 */
@Cancelable
public class TemperatureDamageEvent extends LivingHurtEvent
{
    public TemperatureDamageEvent(LivingEntity entity, DamageSource source, float amount)
    {
        super(entity, source, amount);
    }
}
