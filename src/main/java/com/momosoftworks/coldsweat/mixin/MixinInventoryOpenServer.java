package com.momosoftworks.coldsweat.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.passive.horse.AbstractChestedHorseEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes a Forge oversight that causes the PlayerContainerEvent.Open event to not fire when a player opens their inventory.
 */
@Mixin(ServerPlayNetHandler.class)
class MixinInventoryOpenServer
{
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "handlePlayerCommand(Lnet/minecraft/network/play/client/CEntityActionPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;resetLastActionTime()V"))
    private void onPlayerInvOpen(CEntityActionPacket packet, CallbackInfo ci)
    {
        if (packet.getAction() == CEntityActionPacket.Action.OPEN_INVENTORY
        && !(player.getVehicle() instanceof AbstractChestedHorseEntity))
        {
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
        }
    }
}

@Mixin(Minecraft.class)
class MixinInventoryOpenClient
{
    @Shadow
    public ClientPlayerEntity player;

    @Inject(method = "handleKeybinds()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/Tutorial;onOpenInventory()V"))
    private void onPlayerInvOpen(CallbackInfo ci)
    {
        player.sendOpenInventory();
    }
}
