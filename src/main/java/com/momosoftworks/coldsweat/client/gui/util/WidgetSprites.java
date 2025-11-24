package com.momosoftworks.coldsweat.client.gui.util;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WidgetSprites
{
    ResourceLocation enabled;
    ResourceLocation disabled;
    ResourceLocation enabledFocused;
    ResourceLocation disabledFocused;

    public WidgetSprites(ResourceLocation enabled, ResourceLocation disabled,
                         ResourceLocation enabledFocused, ResourceLocation disabledFocused)
    {
        this.enabled = enabled;
        this.disabled = disabled;
        this.enabledFocused = enabledFocused;
        this.disabledFocused = disabledFocused;
    }

    public WidgetSprites(ResourceLocation unfocused, ResourceLocation focused)
    {   this(unfocused, unfocused, focused, focused);
    }

    public WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused)
    {   this(enabled, disabled, enabledFocused, disabled);
    }

    public ResourceLocation get(boolean enabled, boolean focused)
    {
        if (enabled)
        {   return focused ? this.enabledFocused : this.enabled;
        }
        else
        {   return focused ? this.disabledFocused : this.disabled;
        }
    }
}