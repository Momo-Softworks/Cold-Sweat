package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.common.PlayerAboutToRespawnEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PlayerList.class)
public class MixinPrePlayerRespawn
{
    @Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/server/ServerWorld;addRespawnedPlayer(Lnet/minecraft/entity/player/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onPrePlayerRespawn(ServerPlayerEntity player, boolean returningFromEnd, CallbackInfoReturnable<ServerPlayerEntity> cir,
                                    // Locals
                                    BlockPos respawnPos, float respawnAngle, boolean respawnForced, ServerWorld respawnDimension, Optional<Vector3d> safeRespawnPos, ServerWorld defaultRespawnWorld,
                                    PlayerInteractionManager playerinteractionmanager, ServerPlayerEntity newPlayer)
    {   MinecraftForge.EVENT_BUS.post(new PlayerAboutToRespawnEvent(newPlayer, player, returningFromEnd));
    }
}
