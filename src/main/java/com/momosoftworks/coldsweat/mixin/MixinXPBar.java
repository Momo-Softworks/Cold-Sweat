package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    @ModifyArg(method = "renderExperienceBar",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;draw(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFI)I"),
               index = 3)
    private float offsetExperienceLevel(float y)
    {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? y + 4 : y;
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        // Increasing the 59 in "screenHeight - 59" shifts the item name up
        @ModifyConstant(method = "renderSelectedItemName(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                        constant = @Constant(intValue = 59))
        private int offsetItemNameShift(int yShift)
        {   return ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get() ? yShift + 4 : yShift;
        }
    }
}
