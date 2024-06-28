package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.common.PlayerAboutToRespawnEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PlayerList.class)
public class MixinPrePlayerRespawn
{
    //@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    //private void onPrePlayerRespawn(ServerPlayer player, boolean returningFromEnd, CallbackInfoReturnable<ServerPlayer> cir,
    //                                // Locals
    //                                BlockPos respawnPos, float respawnAngle, boolean respawnForced, ServerLevel respawnDimension, Optional<Vec3> safeRespawnPos, ServerLevel defaultRespawnWorld,
    //                                ServerPlayer newPlayer)
    //{   NeoForge.EVENT_BUS.post(new PlayerAboutToRespawnEvent(newPlayer, player, returningFromEnd));
    //}
}
