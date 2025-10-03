package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeMineSpeedEffect extends TempEffect
{
    public FreezeMineSpeedEffect(TempEffectType<?> type, LivingEntity entity, IntegerBounds bounds)
    {   super(type, entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        if (!this.test(event.getEntity())) return;

        float miningSpeed = 1 - ConfigSettings.COLD_MINING_IMPAIRMENT.get().floatValue();
        if (miningSpeed == 1) return;

        float effect = (float) this.getEffectFactor();
        float minMiningSpeed = CSMath.blend(1, miningSpeed, effect, 0, 1);
        event.setNewSpeed(event.getNewSpeed() * minMiningSpeed);
    }

    @Override
    public Side getSide()
    {   return Side.SERVER;
    }

}
