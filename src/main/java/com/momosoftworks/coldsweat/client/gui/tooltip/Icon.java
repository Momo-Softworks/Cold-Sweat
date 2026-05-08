package com.momosoftworks.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

public final class Icon
{
    private static Icon INSULATION_NORMAL = new Icon(new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar.png"), 8, 8, 28, 0, 36, 28);
    private static Icon INSULATION_CONTRAST = new Icon(new ResourceLocation("cold_sweat:textures/gui/tooltip/insulation_bar_hc.png"), 8, 8, 28, 0, 36, 28);

    public static Supplier<Icon> INSULATION = () -> ConfigSettings.HIGH_CONTRAST.get()
                                                    ? INSULATION_CONTRAST
                                                    : INSULATION_NORMAL;
    private final ResourceLocation location;
    private final int width;
    private final int height;
    private final int u;
    private final int v;
    private final int texWidth;
    private final int texHeight;

    public Icon(ResourceLocation location, int width, int height, int u, int v, int texWidth, int texHeight)
    {
        this.location = location;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }

    public void render(MatrixStack poseStack, int x, int y, int z)
    {
        Minecraft.getInstance().textureManager.bind(location);
        Screen.blit(poseStack, x, y, z, u, v, width, height, texHeight, texWidth);
    }

    public ResourceLocation location()
    {   return location;
    }
    public int width()
    {   return width;
    }
    public int height()
    {   return height;
    }
    public int u()
    {   return u;
    }
    public int v()
    {   return v;
    }
    public int texWidth()
    {   return texWidth;
    }
    public int texHeight()
    {   return texHeight;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Icon that = (Icon) obj;
        return Objects.equals(this.location, that.location) &&
                this.width == that.width &&
                this.height == that.height &&
                this.u == that.u &&
                this.v == that.v &&
                this.texWidth == that.texWidth &&
                this.texHeight == that.texHeight;
    }

    @Override
    public int hashCode()
    {   return Objects.hash(location, width, height, u, v, texWidth, texHeight);
    }
}
