package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackRegistries.class)
public class MixinModLoading
{
    @Inject(method = "loadResources", at = @At("HEAD"))
    private static <D, R> void onWorldLoad(List<IResourcePack> resourcePacks, Commands.EnvironmentType environmentType,
                                           int something, Executor somethingElse, Executor anotherThing,
                                           CallbackInfoReturnable<CompletableFuture<DataPackRegistries>> cir)
    {   ConfigLoadingHandler.initRegistries();
    }

    @Mixin(Minecraft.class)
    public static class Client
    {
        @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
        private void onConnectServer(CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }
    }
}