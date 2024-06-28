package com.momosoftworks.coldsweat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes a Forge oversight that causes the PlayerContainerEvent.Open event to not fire when a player opens their inventory.
 */
@Mixin(ServerGamePacketListenerImpl.class)
class MixinInventoryOpenServer
{
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerCommand(Lnet/minecraft/network/protocol/game/ServerboundPlayerCommandPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"))
    private void onPlayerInvOpen(ServerboundPlayerCommandPacket packet, CallbackInfo ci)
    {
        if (packet.getAction() == ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY
        && !(player.getVehicle() instanceof AbstractHorse))
        {
            NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
        }
    }
}

@Mixin(Minecraft.class)
class MixinInventoryOpenClient
{
    @Shadow
    public LocalPlayer player;

    @Inject(method = "handleKeybinds()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onOpenInventory()V"))
    private void onPlayerInvOpen(CallbackInfo ci)
    {   player.sendOpenInventory();
    }
}
