package net.momostudios.coldsweat.common.event;

import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.ConfigSyncMessage;

public class ConfigSyncEventHandler
{
    @Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
    public static class ServersideEvents
    {
        @SubscribeEvent
        public static void overrideClientConfig(PlayerEvent.PlayerLoggedInEvent event)
        {
            if (!event.getPlayer().world.isRemote)
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) event.getPlayer()),
                        new ConfigSyncMessage(ColdSweatConfig.getInstance()));
        }
    }

    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class ClientsideEvents
    {
        @SubscribeEvent
        public static void overrideServerConfig(PlayerEvent.PlayerLoggedOutEvent event)
        {
            ColdSweatConfig.restoreConfigReference();
        }
    }
}
