package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.momosoftworks.coldsweat.client.gui.tooltip.ClientSoulspringTooltip;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector2i;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderSoulLampInsertionTooltip
{
    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderInsertTooltip(ScreenEvent.Render.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP)
            {
                float fuel = screen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");
                ItemStack carriedStack = screen.getMenu().getCarried();

                if (!carriedStack.isEmpty() && ConfigSettings.LAMP_FUEL_ITEMS.get().containsKey(carriedStack.getItem()))
                {
                    int fuelValue = screen.getMenu().getCarried().getCount() * ConfigSettings.LAMP_FUEL_ITEMS.get().get(carriedStack.getItem());
                    int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                    int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                    GuiGraphics graphics = event.getGuiGraphics();
                    PoseStack ps = graphics.pose();
                    if (event.getMouseY() < slotY + 8)
                    {   ps.translate(0, 32, 0);
                    }

                    graphics.renderTooltip(Minecraft.getInstance().font, List.of(Component.literal("       ")), Optional.empty(), slotX - 18, slotY + 1);

                    RenderSystem.defaultBlendFunc();

                    // Render background
                    graphics.blit(ClientSoulspringTooltip.TOOLTIP_LOCATION.get(), slotX - 7, slotY - 11, 401, 0, 0, 30, 8, 30, 34);

                    // Render ghost overlay
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f + (float) ((Math.sin(FUEL_FADE_TIMER / 5f) + 1f) / 2f) * 0.4f);
                    graphics.blit(ClientSoulspringTooltip.TOOLTIP_LOCATION.get(), slotX - 7, slotY - 11, 401, 0, 8, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 30, 34);
                    RenderSystem.disableBlend();

                    // Render fuel
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
                    graphics.blit(ClientSoulspringTooltip.TOOLTIP_LOCATION.get(), slotX - 7, slotY - 11, 401, 0, 16, (int) (fuel / 2.1333f), 8, 30, 34);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {   FUEL_FADE_TIMER++;
        }
    }
}
