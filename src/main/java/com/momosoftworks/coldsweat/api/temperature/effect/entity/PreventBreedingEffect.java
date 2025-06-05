package com.momosoftworks.coldsweat.api.temperature.effect.entity;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PreventBreedingEffect extends TempEffect
{
    public PreventBreedingEffect(LivingEntity entity, IntegerBounds range)
    {   super(entity, range);
    }

    @SubscribeEvent
    public void onEntityBreed(BabyEntitySpawnEvent event)
    {
        if (this.test(event.getParentA()) || this.test(event.getParentB()))
        {   event.setCanceled(true);
        }
    }

    @Override
    public boolean isClient()
    {   return false;
    }
}
