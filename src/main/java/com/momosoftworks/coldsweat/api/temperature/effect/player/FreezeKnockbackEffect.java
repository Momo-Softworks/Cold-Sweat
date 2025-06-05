package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeKnockbackEffect extends TempEffect
{
    public FreezeKnockbackEffect(LivingEntity entity, IntegerBounds bounds)
    {   super(entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerKnockback(LivingKnockBackEvent event)
    {
        if (!this.test(event.getEntity().getLastHurtByMob())) return;
        if (EntityTempManager.isPeacefulMode(this.entity())) return;

        float knockbackReduction = ConfigSettings.COLD_KNOCKBACK_REDUCTION.get().floatValue();
        if (knockbackReduction <= 0 || this.entity().hasEffect(ModEffects.ICE_RESISTANCE) || this.entity().hasEffect(ModEffects.GRACE)) return;

        float temp = (float) this.getTemperature();
        if (temp < -50f)
        {
            // Get protection from armor underwear
            float tempResistance = (float) Temperature.get(this.entity(), Temperature.Trait.COLD_RESISTANCE);
            if (tempResistance != 1f)
            {   event.setStrength(event.getStrength() * CSMath.blend(1f, tempResistance, temp, this.bounds().min(), this.bounds().max()) * knockbackReduction);
            }
        }
    }

    @Override
    public boolean isClient()
    {   return false;
    }
}
