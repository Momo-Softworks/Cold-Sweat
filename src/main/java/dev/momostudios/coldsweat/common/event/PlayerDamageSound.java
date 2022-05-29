package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(ModDamageSources.COLD))
        {
            if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide)
            {
                WorldHelper.playEntitySound(ModSounds.FREEZE, event.getEntity(), 1.5f, (float) Math.random() / 5f + 0.9f);
            }
        }
    }
}
