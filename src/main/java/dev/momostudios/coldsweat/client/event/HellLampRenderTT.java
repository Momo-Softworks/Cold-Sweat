package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.api.event.client.RenderTooltipPostEvent;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HellLampRenderTT
{
    static int FUEL_FADE_TIMER = 0;
    public static List<Item> VALID_FUEL = new ArrayList<>();
    static
    {
        for (String itemID : ItemSettingsConfig.getInstance().soulLampItems())
        {
            VALID_FUEL.addAll(ConfigHelper.getItems(itemID));
        }
    }

    @SubscribeEvent
    public static void renderInsertTooltip(ScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.HELLSPRING_LAMP)
            {
                float fuel = screen.getSlotUnderMouse().getItem().getOrCreateTag().getFloat("fuel");
                if (!screen.getMenu().getCarried().isEmpty() && VALID_FUEL.contains(screen.getMenu().getCarried().getItem()))
                {
                    int fuelValue = screen.getMenu().getCarried().getCount();
                    int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                    int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                    PoseStack ps = event.getPoseStack();
                    if (event.getMouseY() < slotY + 8)
                        ps.translate(0, 32, 0);

                    event.getScreen().renderComponentTooltip(event.getPoseStack(), List.of(new TextComponent("       ")), slotX - 18, slotY + 1);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 0, 30, 8, 30, 24);
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f + (float) ((Math.sin(FUEL_FADE_TIMER / 5f) + 1f) / 2f) * 0.4f);
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 8, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 30, 24);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 16, (int) (fuel / 2.1333f), 8, 30, 24);
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

            RenderSystem.setShaderTexture(0, new ResourceLocation("cold_sweat:textures/gui/tooltip/soulspring_lamp_fuel.png"));
            GuiComponent.blit(ps, event.getX(), event.getY(), 0, 0, 0, 30, 8, 30, 24);
            GuiComponent.blit(ps, event.getX(), event.getY(), 0, 0, 16, (int) (fuel / 2.1333f), 8, 30, 24);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            FUEL_FADE_TIMER++;
        }
    }
}
