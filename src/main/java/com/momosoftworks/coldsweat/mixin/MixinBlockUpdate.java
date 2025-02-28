package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.BlockStateChangedEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class MixinBlockUpdate
{
    @Mixin(ServerLevel.class)
    public static final class Server
    {
        ServerLevel level = (ServerLevel) (Object) this;

        @Inject(method = "onBlockStateChange", at = @At("HEAD"))
        private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci)
        {
            if (!oldState.equals(newState))
            {   level.getServer().execute(() -> NeoForge.EVENT_BUS.post(new BlockStateChangedEvent(pos, level, oldState, newState)));
            }
        }
    }

    @Mixin(ClientLevel.class)
    public static final class Client
    {
        @Inject(method = "sendBlockUpdated", at = @At("HEAD"))
        private void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci)
        {   NeoForge.EVENT_BUS.post(new BlockStateChangedEvent(pos, (ClientLevel) (Object) this, oldState, newState));
        }
    }
}