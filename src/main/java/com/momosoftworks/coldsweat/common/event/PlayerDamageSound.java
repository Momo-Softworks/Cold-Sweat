package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.sounds.SoundEvents;
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
