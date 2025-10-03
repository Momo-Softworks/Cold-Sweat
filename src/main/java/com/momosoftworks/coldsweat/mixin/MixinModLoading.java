package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EventBus.class)
public class MixinModLoading
{
    @Inject(method = "start", at = @At("HEAD"), remap = false)
    private void onWorldLoad(CallbackInfo ci)
    {   //ConfigLoadingHandler.initRegistries();
    }

    @Mixin(Minecraft.class)
    public static class Client
    {
        /*@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
        private void onConnectServer(CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }*/
    }
}