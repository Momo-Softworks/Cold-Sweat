package com.momosoftworks.coldsweat.api.temperature.effect.player;

import com.momosoftworks.coldsweat.api.temperature.effect.TempEffect;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreezeMineSpeedEffect extends TempEffect
{
    public FreezeMineSpeedEffect(LivingEntity entity, IntegerBounds bounds)
    {   super(entity, bounds);
    }

    @SubscribeEvent
    public void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        if (!this.test(event.getEntity())) return;
        Player player = event.getEntity();
        if (EntityTempManager.isPeacefulMode(player)) return;

        float miningSpeed = 1 - ConfigSettings.COLD_MINING_IMPAIRMENT.get().floatValue();

        if (miningSpeed == 1
        || player.hasEffect(ModEffects.ICE_RESISTANCE)
        || player.hasEffect(ModEffects.GRACE)) return;

        float temp = (float) this.getTemperature();
        // If the player is too cold, slow down their mining speed
        float minMiningSpeed = (float) CSMath.blend(miningSpeed, 1f, Temperature.get(player, Temperature.Trait.COLD_RESISTANCE), 0, 1);
        // Get protection from armor underwear
        event.setNewSpeed(event.getNewSpeed() * CSMath.blend(1f, minMiningSpeed, temp, this.bounds().min(), this.bounds().max()));
    }

    @Override
    protected boolean isClient()
    {   return false;
    }
}
