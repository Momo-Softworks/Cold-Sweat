package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isClientSide && event.getEntity() instanceof Player
        && ConfigSettings.GRACE_ENABLED.get() && !event.getEntity().getPersistentData().getBoolean("GivenGracePeriod"))
        {
            event.getEntity().getPersistentData().putBoolean("GivenGracePeriod", true);
            ((Player) event.getEntity()).addEffect(new MobEffectInstance(ModEffects.GRACE, ConfigSettings.GRACE_LENGTH.get(), 0, false, false, true));
        }
    }
}
