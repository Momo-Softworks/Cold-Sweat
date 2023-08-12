package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.matrix.MatrixStack;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulspringTooltip extends Tooltip
{
    double fuel;

    public SoulspringTooltip(double fuel)
    {
        this.fuel = fuel;
    }

    @Override
    public int getHeight()
    {   return Screen.hasShiftDown() ? CSMath.ceil(ConfigSettings.LAMP_FUEL_ITEMS.get().size() / 6d) * 16 + 14 : 12;
    }

    @Override
    public int getWidth(FontRenderer font)
    {   return Screen.hasShiftDown() ? Math.min(6, ConfigSettings.LAMP_FUEL_ITEMS.get().size()) * 16 : 32;
    }

    @Override
    public void renderImage(FontRenderer font, int x, int y, MatrixStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        y += 11;
        if (Minecraft.getInstance().screen instanceof ContainerScreen<?>)
        {
            Minecraft.getInstance().textureManager.bind(new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
            AbstractGui.blit(poseStack, x, y, 0, 0, 0, 30, 8, 34, 30);
            AbstractGui.blit(poseStack, x, y, 0, 0, 16, (int) (fuel / 2.1333), 8, 34, 30);
            if (Screen.hasShiftDown())
            {
                AbstractGui.blit(poseStack, x + 34, y, 0, 0, 24, 16, 10, 34, 30);

                int i = 0;
                for (Item item : ConfigSettings.LAMP_FUEL_ITEMS.get().keySet())
                {
                    Minecraft.getInstance().getItemRenderer().blitOffset += 401;
                    Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item.getDefaultInstance(), x + ((i * 16) % 96), y + 12 + CSMath.floor(i / 6d) * 16);
                    i++;
                }
            }
        }
    }
}
