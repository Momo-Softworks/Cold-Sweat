package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.momostudios.coldsweat.core.network.message.PlayerModifiersSyncMessage;
import net.momostudios.coldsweat.core.network.message.PlayerTempSyncMessage;
import net.momostudios.coldsweat.core.util.PlayerHelper;
import net.momostudios.coldsweat.core.util.PlayerTemp;

@Mod.EventBusSubscriber
public class TemperatureClientSync
{
    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event)
    {
        if (event.world.getGameTime() % 10 == 0 && !event.world.isRemote)
        {
            for (PlayerEntity player : event.world.getPlayers())
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                        new PlayerTempSyncMessage(
                                PlayerTemp.getTemperature(player, PlayerTemp.Types.BODY).get(),
                                PlayerTemp.getTemperature(player, PlayerTemp.Types.BASE).get()));
            }
        }

        if (event.world.getGameTime() % 60 == 0 && !event.world.isRemote)
        {
            for (PlayerEntity player : event.world.getPlayers())
            {
                PlayerHelper.updateModifiers(player);
            }
        }
    }
}
