package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModEffects;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import glitchcore.event.TickEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class FreezeMoveSpeedEffect extends TempEffect
{
    public FreezeMoveSpeedEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event)
    {
        if (!this.test(event.getEntity())) return;
        if (EntityTempManager.isPeacefulMode(this.entity())) return;

        float temp = (float) this.getTemperature();
        if (temp < -50)
        {
            double movementReduction = ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get();
            double movementSpeed = 1 - movementReduction;

            if (movementSpeed == 1
            || this.entity().hasEffect(ModEffects.ICE_RESISTANCE)
            || this.entity().hasEffect(ModEffects.GRACE)) return;

            // If not elytra flying
            if (!this.entity().isFallFlying())
            {
                // Get protection from armor underwear
                float minMoveMultiplier = (float) CSMath.blend(this.entity().onGround() ? movementSpeed : movementSpeed * 1.25, 1d, Temperature.get(this.entity(), Temperature.Trait.COLD_RESISTANCE), 0d, 1d);
                if (minMoveMultiplier != 1)
                {
                    float moveSpeed = CSMath.blend(1f, minMoveMultiplier, temp, this.bounds().min(), this.bounds().max());
                    this.entity().setDeltaMovement(this.entity().getDeltaMovement().multiply(moveSpeed, 1, moveSpeed));
                }
            }
        }
    }

    @Override
    public boolean isClient()
    {   return false;
    }
}
