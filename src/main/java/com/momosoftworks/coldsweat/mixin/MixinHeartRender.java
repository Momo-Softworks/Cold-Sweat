package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.api.event.vanilla.RenderHeartEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class MixinHeartRender
{
    @Inject(method = "extractHeart", at = @At("TAIL"))
    private void renderHeart(GuiGraphicsExtractor graphics, Hud.HeartType heartType, int x, int y, boolean hardcore, boolean blink, boolean halfHeart, CallbackInfo ci)
    {
        NeoForge.EVENT_BUS.post(new RenderHeartEvent(graphics, heartType, x, y, hardcore, blink, halfHeart));
    }
}
