package com.momosoftworks.coldsweat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes a Forge oversight that causes the PlayerContainerEvent.Open event to not fire when a player opens their inventory.
 */
@Mixin(ServerGamePacketListenerImpl.class)
class MixinInventoryOpenServer
{
    ServerGamePacketListenerImpl self = (ServerGamePacketListenerImpl)(Object)this;

    @Inject(method = "handlePlayerCommand(Lnet/minecraft/network/protocol/game/ServerboundPlayerCommandPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void onPlayerInvOpen(ServerboundPlayerCommandPacket packet, CallbackInfo ci)
    {
        Player player = self.getPlayer();
        if (packet.getAction() == ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY
        && !(player.getVehicle() instanceof AbstractHorse))
        {
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
        }
    }
}

@Mixin(Minecraft.class)
class MixinInventoryOpenClient
{
    Minecraft self = (Minecraft)(Object)this;

    @Inject(method = "handleKeybinds()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onOpenInventory()V"))
    private void onPlayerInvOpen(CallbackInfo ci)
    {   self.player.sendOpenInventory();
    }
}
