package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityMoveEvent;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeMoveSpeedEffect extends TempEffect
{
    public FreezeMoveSpeedEffect(TempEffectType<?> type, IntegerBounds bounds)
    {   super(type, bounds);
    }

    @SubscribeEvent
    public void onPlayerTick(EntityMoveEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!this.test(entity)) return;

        float effect = (float) this.getEffectFactor(entity);
        double movementReduction = ConfigSettings.COLD_MOVEMENT_SLOWDOWN.get();
        if (movementReduction == 0) return;

        float movePenalty = (float) CSMath.blend(0, movementReduction, effect, 0, 1);
        if (movePenalty != 0)
        {
            if (entity.isSprinting() && !entity.onGround())
            {   movePenalty *= 1.5f;
            }
            event.setSpeed(event.getSpeed() * (1-Math.min(1, movePenalty)));
            event.setCanceled(true);
        }
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }
}
