package com.momosoftworks.coldsweat.api.temperature.effect.entity;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PreventBreedingEffect extends TempEffect
{
    public PreventBreedingEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    @SubscribeEvent
    public void onEntityBreed(BabyEntitySpawnEvent event)
    {
        if (!this.test(event.getParentA()) || !this.test(event.getParentB()))
        {   event.setCanceled(true);
        }
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }
}
