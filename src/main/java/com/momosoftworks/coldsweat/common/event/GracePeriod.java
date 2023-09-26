package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isClientSide && event.getEntity() instanceof PlayerEntity
        && ConfigSettings.GRACE_ENABLED.get() && !event.getEntity().getPersistentData().getBoolean("GivenGracePeriod"))
        {
            event.getEntity().getPersistentData().putBoolean("GivenGracePeriod", true);
            ((PlayerEntity) event.getEntity()).addEffect(new EffectInstance(ModEffects.GRACE, ConfigSettings.GRACE_LENGTH.get(), 0, false, false, true));
        }
    }
}
