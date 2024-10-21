package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.ServerConfigsLoadedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Must be present because biome modifiers are loaded before {@link net.minecraftforge.event.server.ServerAboutToStartEvent} is fired, and configs must load before that point.
 */
@Mixin(ServerLifecycleHooks.class)
public class MixinPreServerLoad
{
    @Inject(method = "handleServerAboutToStart", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/config/ConfigTracker;loadConfigs(Lnet/minecraftforge/fml/config/ModConfig$Type;Ljava/nio/file/Path;)V"), remap = false)
    private static void onPreServerLoad(MinecraftServer server, CallbackInfoReturnable<Boolean> cir)
    {   MinecraftForge.EVENT_BUS.post(new ServerConfigsLoadedEvent(server));
    }
}
