package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.registries.ModDamageSources;
import com.momosoftworks.coldsweat.util.registries.ModSounds;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
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
            if (event.getEntity() instanceof PlayerEntity && !event.getEntity().level.isClientSide)
            {
                PlayerEntity player = (PlayerEntity) event.getEntity();
                WorldHelper.playEntitySound(ModSounds.FREEZE_DAMAGE, player, player.getSoundSource(), 2f, EntityHelper.getVoicePitch(player));
            }
        }
    }
}
