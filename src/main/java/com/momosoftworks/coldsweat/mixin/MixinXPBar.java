package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Hud.class)
public class MixinXPBar
{
    @Inject(method = "extractExperienceLevel", at = @At("HEAD"))
    public void renderExperienceBar1(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci)
    {
        // Render XP bar
        if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
        {   graphics.pose().pushMatrix();
            graphics.pose().translate(0, 4);
        }
    }

    @Inject(method = "extractExperienceLevel", at = @At("TAIL"))
    public void renderExperienceBar2(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci)
    {
        // Render XP bar
        if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
        {   graphics.pose().popMatrix();
        }
    }

    @Mixin(Hud.class)
    public static class MixinItemLabel
    {
        @Inject(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V", at = @At("HEAD"))
        public void renderItemNamePre1(GuiGraphicsExtractor graphics, int yShift, CallbackInfo ci)
        {
            // Render XP bar
            if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
            {   graphics.pose().pushMatrix();
                graphics.pose().translate(0, -4);
            }
        }
        @Inject(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V", at = @At("TAIL"))
        public void renderItemNamePre2(GuiGraphicsExtractor graphics, int yShift, CallbackInfo ci)
        {
            // Render XP bar
            if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
            {   graphics.pose().popMatrix();
            }
        }
    }
}
