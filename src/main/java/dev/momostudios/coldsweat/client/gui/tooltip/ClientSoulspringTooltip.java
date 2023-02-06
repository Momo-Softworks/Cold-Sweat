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
        return Screen.hasShiftDown() ? CSMath.ceil(ConfigSettings.LAMP_FUEL_ITEMS.get().size() / 6d) * 32 + 16 : 14;
    }

    @Override
    public int getWidth(Font font)
    {
        return Screen.hasShiftDown() ? Math.min(6, ConfigSettings.LAMP_FUEL_ITEMS.get().size()) * 16 : 32;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer)
    {
        if (!Screen.hasShiftDown())
        {
            font.drawInBatch("§9? §8'Shift'", x + 34, y + 1, 0, false, matrix, buffer, false, 0, 15728880);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int depth)
    {
        List<Item> fuelItems = ConfigSettings.LAMP_FUEL_ITEMS.get();
        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
        GuiComponent.blit(poseStack, x, y + 1, 0, 0, 0, 30, 8, 30, 34);
        GuiComponent.blit(poseStack, x, y + 1, 0, 0, 16, (int) (fuel / 2.1333), 8, 30, 34);
        if (Screen.hasShiftDown())
        {
            GuiComponent.blit(poseStack, x + 34, y + 1, 0, 0, 24, 16, 10, 30, 34);

            for (int i = 0; i < fuelItems.size(); i++)
            {
                itemRenderer.renderGuiItem(fuelItems.get(i).getDefaultInstance(), x + ((i * 16) % 96), y + 13 + CSMath.floor(i / 6d) * 16);
            }
        }
    }
}
