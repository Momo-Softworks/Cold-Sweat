package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Must be present because biome modifiers are loaded before {@link net.neoforged.neoforge.event.server.ServerAboutToStartEvent} is fired, and configs must load before that point.
 */
@Mixin(ServerLifecycleHooks.class)
public class MixinPreServerLoad
{
    @Inject(method = "handleServerAboutToStart", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/config/ConfigTracker;loadConfigs(Lnet/neoforged/fml/config/ModConfig$Type;Ljava/nio/file/Path;Ljava/nio/file/Path;)V"), remap = false)
    private static void onPreServerLoad(MinecraftServer server, CallbackInfo ci)
    {   NeoForge.EVENT_BUS.post(new ServerConfigsLoadedEvent(server));
    }
}
