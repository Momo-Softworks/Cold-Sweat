package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record Icon(ResourceLocation location, int width, int height, int u, int v, int texWidth, int texHeight)
{
    private static Icon INSULATION_NORMAL = new Icon(new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"), 8, 8, 28, 0, 36, 28);
    private static Icon INSULATION_CONTRAST = new Icon(new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png"), 8, 8, 28, 0, 36, 28);

    public static Supplier<Icon> INSULATION = () -> ConfigSettings.HIGH_CONTRAST.get() ? INSULATION_CONTRAST : INSULATION_NORMAL;

    public void render(PoseStack poseStack, int x, int y, int z)
    {
        RenderSystem.setShaderTexture(0, location);
        Screen.blit(poseStack, x, y, z, u, v, width, height, 36, 28);
    }
}
