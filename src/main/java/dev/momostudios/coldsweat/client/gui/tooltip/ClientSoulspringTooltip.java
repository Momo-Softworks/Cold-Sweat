package dev.momostudios.coldsweat.client.gui.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientSoulspringTooltip implements ClientTooltipComponent
{
    double fuel;

    public ClientSoulspringTooltip(double fuel)
    {
        this.fuel = fuel;
    }

    @Override
    public int getHeight()
    {
        return 0;
    }

    @Override
    public int getWidth(Font font)
    {
        return 0;
    }

    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        List<Item> fuelItems = ConfigSettings.LAMP_FUEL_ITEMS.get();
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
        GuiComponent.blit(poseStack, x, y, 0, 0, 0, 30, 8, 30, 34);
        GuiComponent.blit(poseStack, x, y, 0, 0, 16, (int) (fuel / 2.1333), 8, 30, 34);
        if (Screen.hasShiftDown())
        {
            GuiComponent.blit(poseStack, x + 34, y, 0, 0, 24, 16, 10, 30, 34);

            for (int i = 0; i < fuelItems.size(); i++)
            {
                itemRenderer.renderGuiItem(fuelItems.get(i).getDefaultInstance(), x + ((i * 16) % 96), y + 13 + CSMath.floor(i / 6d) * 16);
            }
        }
    }
}
