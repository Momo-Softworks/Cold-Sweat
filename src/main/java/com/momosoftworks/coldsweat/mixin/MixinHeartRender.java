package com.momosoftworks.coldsweat.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.api.event.vanilla.RenderHeartEvent;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinHeartRender
{
    @Inject(method = "renderHeart", at = @At("TAIL"))
    private void renderHeart(PoseStack ps, Gui.HeartType heartType, int x, int y, int yOffset, boolean blink, boolean halfHeart, CallbackInfo ci)
    {
        MinecraftForge.EVENT_BUS.post(new RenderHeartEvent(ps, heartType, x, y, yOffset, blink, halfHeart));
    }
}
