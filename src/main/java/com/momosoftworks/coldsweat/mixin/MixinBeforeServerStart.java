package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLifecycleHooks.class)
public class MixinBeforeServerStart
{
    @Inject(method = "handleServerAboutToStart", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/server/ServerLifecycleHooks;runModifiers(Lnet/minecraft/server/MinecraftServer;)V"), remap = false)
    private static void beforeServerStart(MinecraftServer server, CallbackInfo ci)
    {   ConfigSettings.load(server.registryAccess());
    }
}
