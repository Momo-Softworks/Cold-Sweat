package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(WorldLoader.class)
public class MixinModLoading
{
    @Inject(method = "load", at = @At("HEAD"))
    private static <D, R> void onWorldLoad(WorldLoader.InitConfig initConfig, WorldLoader.WorldDataSupplier<D> worldDate,
                                           WorldLoader.ResultFactory<D, R> something, Executor somethingElse, Executor anotherThing,
                                           CallbackInfoReturnable<CompletableFuture<R>> cir)
    {   ConfigLoadingHandler.initRegistries();
    }

    @Mixin(Minecraft.class)
    public static class Client
    {
        @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
        private void onConnectServer(CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }
    }
}