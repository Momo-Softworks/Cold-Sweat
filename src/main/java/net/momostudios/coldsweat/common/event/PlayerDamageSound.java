package net.momostudios.coldsweat.common.event;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.PlaySoundMessage;
import net.momostudios.coldsweat.util.CustomDamageTypes;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CustomDamageTypes.COLD) || event.getSource().equals(CustomDamageTypes.COLD_SCALED))
        {
            if (event.getEntity() instanceof PlayerEntity && !event.getEntity().world.isRemote)
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new PlaySoundMessage(0, 1f, (float) Math.random() * 0.3f + 0.85f, event.getEntity().getUniqueID()));
            }
        }
    }
}
