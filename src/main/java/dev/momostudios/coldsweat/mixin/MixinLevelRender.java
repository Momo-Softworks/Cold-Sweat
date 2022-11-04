package dev.momostudios.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.event.client.RenderLevelEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001)
public class MixinLevelRender
{
    @Shadow
    private Frustum cullingFrustum;

    @Inject(method = "renderLevel", at = @At("HEAD"), remap = ColdSweat.REMAP_MIXINS, cancellable = true)
    void renderLevel(PoseStack ps, float partialTicks, long finishTimeNano, boolean renderBlockOutline, Camera camera,
                     GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci)
    {
        RenderLevelEvent event = new RenderLevelEvent(ps, partialTicks, finishTimeNano, renderBlockOutline, camera,
                (LevelRenderer) (Object) this, gameRenderer, lightTexture, matrix4f, cullingFrustum);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) ci.cancel();
    }
}
