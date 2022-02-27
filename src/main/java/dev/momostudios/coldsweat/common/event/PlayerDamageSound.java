package dev.momostudios.coldsweat.common.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.PlaySoundMessage;
import dev.momostudios.coldsweat.util.CSDamageTypes;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber
public class PlayerDamageSound
{
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getSource().equals(CSDamageTypes.COLD) || event.getSource().equals(CSDamageTypes.COLD_SCALED))
        {
            if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide)
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new PlaySoundMessage(0, 1f, (float) Math.random() * 0.3f + 0.85f, event.getEntity().getUUID()));
            }
        }
    }
}
