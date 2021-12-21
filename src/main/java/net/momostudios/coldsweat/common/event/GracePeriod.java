package net.momostudios.coldsweat.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.GracePeriodAskMessage;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

@Mod.EventBusSubscriber
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            if (!Minecraft.getInstance().isSingleplayer() && !event.getEntity().world.isRemote)
                ColdSweatPacketHandler.INSTANCE.sendToServer(new GracePeriodAskMessage(6000));
            else if (!event.getEntity().getPersistentData().getBoolean("givenGracePeriod"))
            {
                event.getEntity().getPersistentData().putBoolean("givenGracePeriod", true);
                ((PlayerEntity) event.getEntity()).addPotionEffect(new EffectInstance(ModEffects.INSULATION, 6000, 4, false, false));
            }
        }
    }
}
