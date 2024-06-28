package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Player
        && ConfigSettings.GRACE_ENABLED.get() && !event.getEntity().getPersistentData().getBoolean("GivenGracePeriod"))
        {
            event.getEntity().getPersistentData().putBoolean("GivenGracePeriod", true);
            ((Player) event.getEntity()).addEffect(new MobEffectInstance(ModEffects.GRACE, ConfigSettings.GRACE_LENGTH.get(), 0, false, false, true));
        }
    }
}
