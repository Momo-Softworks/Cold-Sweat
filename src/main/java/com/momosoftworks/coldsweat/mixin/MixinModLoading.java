package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigLoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinModLoading
{
    @Mixin(Main.class)
    public static class OnServer
    {
        @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getSummary()Lnet/minecraft/world/level/storage/LevelSummary;"))
        private static void beforeServerStart(String[] args, CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }
    }

    @Mixin(Minecraft.class)
    public static class onClient
    {
        @Inject(method = "<init>", at = @At("RETURN"))
        private void beforeClientStart(CallbackInfo ci)
        {   ConfigLoadingHandler.initRegistries();
        }
    }
}