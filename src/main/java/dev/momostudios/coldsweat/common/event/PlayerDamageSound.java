package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.util.entity.ModDamageSources;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
            if (event.getEntity() instanceof Player player && !event.getEntity().level.isClientSide)
            {
                WorldHelper.playEntitySound(SoundEvents.PLAYER_HURT_FREEZE, player, player.getSoundSource(), 2f, player.getVoicePitch());
            }
        }
    }
}
