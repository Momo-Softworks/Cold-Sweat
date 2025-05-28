package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.RenderHeartEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A needlessly complex mixin to render frozen hearts when the player's temp falls below -50
 */
@Mixin(Gui.class)
public class MixinHeartRender
{
    @Inject(method = "renderHeart", at = @At("TAIL"))
    private void renderHeart(GuiGraphics graphics, Gui.HeartType heartType, int x, int y, boolean hardcore, boolean blink, boolean halfHeart, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new RenderHeartEvent(graphics, heartType, x, y, hardcore, blink, halfHeart));
    }
}
