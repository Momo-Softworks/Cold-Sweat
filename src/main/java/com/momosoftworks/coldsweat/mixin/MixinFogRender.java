package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.api.event.client.RenderFogEvent;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FogRenderer.class)
public class MixinFogRender
{
    @Inject(method = "setupFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZF)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;fogStart(F)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void setupFog(ActiveRenderInfo camera, FogRenderer.FogType fogType, float farPlaneDistance, boolean doNearFog, float partialTicks, CallbackInfo ci,
                                 // Locals
                                 FluidState fluidstate, Entity entity, float hook, float nearPlane, float farPlane)
    {
        RenderFogEvent event = new RenderFogEvent(GlStateManager.FogMode.LINEAR, fogType, camera, partialTicks, nearPlane, farPlane);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            RenderSystem.fogStart(event.getNearPlaneDistance());
            RenderSystem.fogEnd(event.getFarPlaneDistance());
            RenderSystem.fogMode(event.getMode());
            RenderSystem.setupNvFogDistance();
            ForgeHooksClient.onFogRender(event.getType(), camera, partialTicks, event.getFarPlaneDistance());
            ci.cancel();
        }
    }
}
