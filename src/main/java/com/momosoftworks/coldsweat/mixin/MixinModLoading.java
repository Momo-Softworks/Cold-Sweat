package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.WorldStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(WorldStem.class)
public class MixinModLoading
{
    @Inject(method = "load", at = @At("HEAD"))
    private static <D, R> void onWorldLoad(WorldStem.InitConfig config, WorldStem.DataPackConfigSupplier dataPackConfigSupplier,
                                           WorldStem.WorldDataSupplier worldDate, Executor something, Executor somethingElse,
                                           CallbackInfoReturnable<CompletableFuture<WorldStem>> cir)
    {   ConfigLoadingHandler.initRegistries();
    }

    @Mixin(Minecraft.class)
    public static class Client
    {
        @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
        private void onConnectServer(CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }
    }
}