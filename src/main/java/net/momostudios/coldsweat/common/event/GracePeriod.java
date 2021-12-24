package net.momostudios.coldsweat.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.common.effect.GraceEffect;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.GracePeriodAskMessage;
import net.momostudios.coldsweat.core.util.registrylists.ModEffects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class GracePeriod
{
    @SubscribeEvent
    public static void onSpawn(EntityJoinWorldEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity && ConfigCache.getInstance().gracePeriodEnabled)
        {
            ColdSweatPacketHandler.INSTANCE.sendToServer(new GracePeriodAskMessage(ConfigCache.getInstance().gracePeriodLength));
        }
    }
}
