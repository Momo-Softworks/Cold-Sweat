package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    @Inject(method = "renderExperienceLevel", at = @At("HEAD"))
    public void renderExperienceBar1(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci)
    {
        // Render XP bar
        if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
        {   graphics.pose().pushPose();
            graphics.pose().translate(0, 4, 0);
        }
    }

    @Inject(method = "renderExperienceLevel", at = @At("TAIL"))
    public void renderExperienceBar2(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci)
    {
        // Render XP bar
        if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
        {   graphics.pose().popPose();
        }
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At("HEAD"))
        public void renderItemNamePre1(GuiGraphics graphics, int yShift, CallbackInfo ci)
        {
            // Render XP bar
            if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
            {   graphics.pose().pushPose();
                graphics.pose().translate(0, -4, 0);
            }
        }
        @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At("TAIL"))
        public void renderItemNamePre2(GuiGraphics graphics, int yShift, CallbackInfo ci)
        {
            // Render XP bar
            if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
            {   graphics.pose().popPose();
            }
        }
    }
}
