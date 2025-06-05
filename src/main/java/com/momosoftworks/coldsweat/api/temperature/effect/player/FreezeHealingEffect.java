package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeHealingEffect extends TempEffect
{
    public FreezeHealingEffect(LivingEntity entity, IntegerBounds bounds)
    {   super(entity, bounds);
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event)
    {
        if (!this.test(event.getEntity())) return;
        if (EntityTempManager.isPeacefulMode(this.entity())) return;

        double frozenHeartsPercentage = ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get();

        if (frozenHeartsPercentage <= 0
        || this.entity().hasEffect(ModEffects.ICE_RESISTANCE)
        || this.entity().hasEffect(ModEffects.GRACE)) return;

        float healing = event.getAmount();
        float temp = (float) this.getTemperature();
        if (temp < -50)
        {
            // Get protection from armor underwear
            float unfrozenHealth = (float) (CSMath.blend(1 - frozenHeartsPercentage, 1d, Temperature.get(this.entity(), Temperature.Trait.COLD_RESISTANCE), 0d, 1d));
            if (unfrozenHealth != 1)
            {   event.setAmount(CSMath.clamp(healing, 0, CSMath.ceil(this.entity().getMaxHealth() * CSMath.blend(unfrozenHealth, 1f, temp, this.bounds().min(), this.bounds().max())) - this.entity().getHealth()));
            }
        }
    }

    @Override
    public boolean isClient()
    {   return false;
    }
}
