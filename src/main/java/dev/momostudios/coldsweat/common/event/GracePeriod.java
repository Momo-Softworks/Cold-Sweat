package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.registries.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Player
        && ConfigSettings.GRACE_ENABLED.get() && !event.getEntity().getPersistentData().getBoolean("givenGracePeriod"))
        {
            event.getEntity().getPersistentData().putBoolean("givenGracePeriod", true);
            ((Player) event.getEntity()).addEffect(new MobEffectInstance(ModEffects.GRACE, ConfigSettings.GRACE_LENGTH.get(), 0, false, false, true));
        }
    }
}
