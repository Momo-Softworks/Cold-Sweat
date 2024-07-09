package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = Gui.class)
public class MixinXPBar
{
    @ModifyVariable(method = "renderExperienceLevel", at = @At(value = "LOAD", ordinal = 0), ordinal = 2)
    public int renderExperienceBar2(int y)
    {
        // Render XP bar
        if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
        {   return y + 4;
        }
        return y;
    }

    @Mixin(Gui.class)
    public static class MixinItemLabel
    {
        @ModifyVariable(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "LOAD"), ordinal = 3)
        public int renderItemNamePre(int y)
        {
            if (ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get())
            {   return y - 4;
            }
            return y;
        }
    }
}
