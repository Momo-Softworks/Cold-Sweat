package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeHealingEffect extends TempEffect
{
    public FreezeHealingEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    @SubscribeEvent
    public void onHeal(LivingHealEvent event)
    {
        LivingEntity entity = event.getEntity();
        if (!this.test(entity)) return;
        double effect = this.getEffectFactor(entity);
        double heartsFreezePercentage = ConfigSettings.HEARTS_FREEZING_PERCENTAGE.get();
        if (heartsFreezePercentage == 0) return;

        float maxHealth = entity.getMaxHealth();

        float maxFrozenHealth = (float) (maxHealth * heartsFreezePercentage);
        float frozenHealth = Math.round(CSMath.blend(0, maxFrozenHealth, effect, 0, 1));
        float unfrozenHealth = maxHealth - frozenHealth;
        // Cap healing to only heal up to the unfrozen health amount
        float healAmount = CSMath.clamp(event.getAmount(), 0, Math.max(0, unfrozenHealth - entity.getHealth()));
        event.setAmount(healAmount);
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }
}
