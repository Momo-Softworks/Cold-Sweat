package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record Icon(ResourceLocation location, int width, int height, int u, int v, int texWidth, int texHeight)
{
    private static Icon INSULATION_NORMAL = new Icon(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/tooltip/insulation_bar.png"), 8, 8, 28, 0, 36, 28);
    private static Icon INSULATION_CONTRAST = new Icon(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "textures/gui/tooltip/insulation_bar_hc.png"), 8, 8, 28, 0, 36, 28);

    public static Supplier<Icon> INSULATION = () -> ConfigSettings.HIGH_CONTRAST.get() ? INSULATION_CONTRAST : INSULATION_NORMAL;

    public void render(GuiGraphics graphics, int x, int y, int z)
    {   graphics.blit(location, x, y, z, u, v, width, height, 36, 28);
    }
}
