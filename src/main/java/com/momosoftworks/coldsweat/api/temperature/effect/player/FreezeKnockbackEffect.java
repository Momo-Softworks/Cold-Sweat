package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeKnockbackEffect extends TempEffect
{
    public FreezeKnockbackEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerKnockback(LivingKnockBackEvent event)
    {
        if (!this.test(event.getEntityLiving().getLastHurtByMob())) return;

        float knockbackReduction = ConfigSettings.COLD_KNOCKBACK_REDUCTION.get().floatValue();
        if (knockbackReduction == 0) return;

        float effect = (float) this.getEffectFactor();
        event.setStrength(event.getStrength() * CSMath.blend(1, 1 - knockbackReduction, effect, 0, 1));
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }
}
