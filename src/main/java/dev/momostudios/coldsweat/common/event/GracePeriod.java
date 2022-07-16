package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.config.ConfigCache;
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
        if (event.getEntity() instanceof Player && ConfigCache.getInstance().graceEnabled)
        {
            if (!event.getWorld().isClientSide && !event.getEntity().getPersistentData().getBoolean("givenGracePeriod"))
            {
                event.getEntity().getPersistentData().putBoolean("givenGracePeriod", true);
                ((Player) event.getEntity()).addEffect(new MobEffectInstance(ModEffects.GRACE, ConfigCache.getInstance().graceLength, 0, false, false));
            }
        }
    }
}
