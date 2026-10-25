package com.momosoftworks.coldsweat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    @ModifyExpressionValue(method = "renderExperienceLevel",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;guiHeight()I"))
    private int offsetExperienceLevel(int guiHeight)
    {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? guiHeight + 4 : guiHeight;
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        @ModifyExpressionValue(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
                               at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
        private int offsetItemName(int yShift)
        {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? yShift + 4 : yShift;
        }
    }
}
