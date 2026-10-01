package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.client.renderer.RenderStateData;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.client.renderer.entity.state.GoatRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GoatRenderer.class)
public class MixinGoatRenderer
{
    private static final Identifier SHEARED_GOAT_TEXTURE = Identifier.withDefaultNamespace("textures/entity/goat/goat_shaven.png");

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/GoatRenderState;)Lnet/minecraft/resources/Identifier;",
            at = @At("HEAD"), cancellable = true)
    private void getTextureLocation(GoatRenderState state, CallbackInfoReturnable<Identifier> cir)
    {
        if (Boolean.TRUE.equals(state.getRenderData(RenderStateData.GOAT_SHEARED)))
        {   cir.setReturnValue(SHEARED_GOAT_TEXTURE);
        }
    }
}
