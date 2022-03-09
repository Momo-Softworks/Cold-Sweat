package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.momostudios.coldsweat.client.event.csevents.RenderTooltipPostEvent;
import dev.momostudios.coldsweat.util.config.ConfigEntry;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HellLampRenderTT
{
    static int time = 0;

    @SubscribeEvent
    public static void renderInsertTooltip(ScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.HELLSPRING_LAMP)
            {
                float fuel = screen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");
                if (!screen.getMenu().getCarried().isEmpty())
                {
                    int fuelValue = getItemEntry(screen.getMenu().getCarried()).value * screen.getMenu().getCarried().getCount();
                    if (fuelValue > 0)
                    {
                        int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                        int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                        PoseStack ps = event.getPoseStack();
                        if (event.getMouseY() < slotY + 8)
                            ps.translate(0, 32, 0);

                        event.getScreen().renderComponentTooltip(event.getPoseStack(), List.of(new TextComponent("       ")), slotX - 18, slotY + 1);

                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
                        GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 0, 30, 8, 30, 8);
                        RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f + (float) ((Math.sin(time / 5f) + 1f) / 2f) * 0.4f);
                        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_ghost.png"));
                        GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 0, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 30, 8);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
                        RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
                        GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 0, (int) (fuel / 2.1333f), 8, 30, 8);

                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderNormalTooltip(RenderTooltipPostEvent event)
    {
        if (event.getItemStack().getItem() == ModItems.HELLSPRING_LAMP)
        {
            ItemStack stack = event.getItemStack();
            float fuel = stack.getOrCreateTag().getFloat("fuel");
            PoseStack ps = event.getPoseStack();

            RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel_empty.png"));
            GuiComponent.blit(ps, event.getX(), event.getY(), 401, 0, 0, 30, 8, 30, 8);
            RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/screen/soulfire_lamp_fuel.png"));
            GuiComponent.blit(ps, event.getX(), event.getY(), 401, 0, 8, (int) (fuel / 2.1333f), 8, 30, 8);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            time = (time + 1) % (int) (5 * (Math.PI * 2));
        }
    }

    public static ConfigEntry getItemEntry(ItemStack stack)
    {
        for (String entry : ItemSettingsConfig.getInstance().soulLampItems())
        {
            if (entry.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()).toString()))
            {
                return new ConfigEntry(entry, 1);
            }
        }
        return new ConfigEntry("minecraft:air", 0);
    }
}
