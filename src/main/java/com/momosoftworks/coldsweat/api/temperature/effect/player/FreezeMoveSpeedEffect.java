package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeMoveSpeedEffect extends TempEffect
{
    public FreezeMoveSpeedEffect(LivingEntity entity, IntegerBounds bounds)
    {   super(entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (!this.test(event.player)) return;
        if (EntityTempManager.isPeacefulMode(this.entity())) return;

        if (event.phase == TickEvent.Phase.END)
        {
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
                    float minMoveMultiplier = (float) CSMath.blend(this.entity().isOnGround() ? movementSpeed : movementSpeed * 1.25, 1d, Temperature.get(this.entity(), Temperature.Trait.COLD_RESISTANCE), 0d, 1d);
                    if (minMoveMultiplier != 1)
                    {
                        float moveSpeed = CSMath.blend(1f, minMoveMultiplier, temp, this.bounds().min(), this.bounds().max());
                        this.entity().setDeltaMovement(this.entity().getDeltaMovement().multiply(moveSpeed, 1, moveSpeed));
                    }
                }
            }
        }
    }

    @Override
    protected boolean isClient()
    {   return false;
    }
}
