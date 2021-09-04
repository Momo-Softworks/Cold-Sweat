package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class TemperatureClientSync
{
    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event)
    {
        if (!event.player.world.isRemote() && event.type != TickEvent.Type.CLIENT && Minecraft.getInstance().getRenderViewEntity() instanceof PlayerEntity
        && event.player.ticksExisted % 2 == 0)
        {
            Minecraft.getInstance().getRenderViewEntity().getPersistentData().putDouble("body_temperature", PlayerTemp.getTemperature(event.player, PlayerTemp.Types.BODY).get());
        }
    }
}
