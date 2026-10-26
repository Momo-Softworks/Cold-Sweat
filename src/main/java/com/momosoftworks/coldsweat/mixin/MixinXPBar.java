package com.momosoftworks.coldsweat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ContextualBar.class)
public interface MixinXPBar
{
    @ModifyExpressionValue(method = "extractExperienceLevel",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;guiHeight()I"))
    private static int offsetExperienceLevel(int guiHeight)
    {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? guiHeight + 4 : guiHeight;
    }

    @Mixin(Hud.class)
    class MixinItemLabel
    {
        @ModifyExpressionValue(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V",
                               at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
        private int offsetItemName(int yShift)
        {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? yShift + 4 : yShift;
        }
    }
}
