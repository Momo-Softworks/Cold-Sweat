package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.client.RenderWorldEvent;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRender
{
    @Shadow
    private ClientWorld level;

    @Inject(method = "renderLevel(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/renderer/culling/ClippingHelper;)V"), remap = ColdSweat.REMAP_MIXINS)
    private void renderLevel(MatrixStack matrixStack, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activeRenderInfo,
                             GameRenderer gameRenderer, LightTexture lightmap, Matrix4f projection, CallbackInfo ci)
    {
        MinecraftForge.EVENT_BUS.post(new RenderWorldEvent(matrixStack, partialTicks, finishTimeNano, drawBlockOutline, activeRenderInfo, gameRenderer, lightmap, projection, level));
    }
}
