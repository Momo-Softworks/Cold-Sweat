package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    @ModifyArg(method = "renderExperienceBar",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"),
               index = 3)
    private int offsetExperienceLevel(int y)
    {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? y + 4 : y;
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        // Shifting both arguments of Math.max(yShift, 59) shifts its result by the same amount
        @ModifyArg(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
                   at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
                   index = 0, remap = false)
        private int offsetItemNameShift(int yShift)
        {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? yShift + 4 : yShift;
        }

        @ModifyArg(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V",
                   at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
                   index = 1, remap = false)
        private int offsetItemNameMinShift(int minShift)
        {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? minShift + 4 : minShift;
        }
    }
}
