package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import com.momosoftworks.coldsweat.client.gui.tooltip.ClientSoulspringTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.SoulspringTooltip;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.event.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Wearable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final ChatFormatting COLD = ChatFormatting.BLUE;
    public static final ChatFormatting HOT = ChatFormatting.RED;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        ItemData itemData = ItemData.of(stack);
        if (stack.isEmpty()) return;

        // Get the index at which the tooltip should be inserted
        int tooltipIndex = Math.min(1, event.getTooltipElements().size() - 1);
        Optional<FormattedText> line;
        while ((line = event.getTooltipElements().get(tooltipIndex).left()).isPresent()
        && !line.get().equals(stack.getDisplayName()))
        {   tooltipIndex++;
            if (tooltipIndex >= event.getTooltipElements().size())
            {   tooltipIndex = Math.min(1, event.getTooltipElements().size());
                break;
            }
        }

        Insulation itemInsul = null;
        // Add the armor insulation tooltip if the armor has insulation
        if (stack.getItem() instanceof SoulspringLampItem)
        {   if (!Screen.hasShiftDown())
            {   event.getTooltipElements().add(tooltipIndex, Either.left(Component.literal("? ").withStyle(ChatFormatting.BLUE).append(Component.literal("'Shift'").withStyle(ChatFormatting.DARK_GRAY))));
            }
            event.getTooltipElements().add(tooltipIndex, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("Fuel"))));
        }
        else if (stack.getUseAnimation() == UseAnim.DRINK || stack.getUseAnimation() == UseAnim.EAT)
        {
            ConfigSettings.FOOD_TEMPERATURES.get().computeIfPresent(ItemData.of(event.getItemStack()), (item, temp) ->
            {
                int index = Minecraft.getInstance().options.advancedItemTooltips ? event.getTooltipElements().size() - 1 : event.getTooltipElements().size();
                event.getTooltipElements().add(index, Either.left(
                        temp > 0 ? Component.translatable("tooltip.cold_sweat.temperature_effect", "+" + temp).withStyle(HOT)
                                 : Component.translatable("tooltip.cold_sweat.temperature_effect", temp).withStyle(COLD)
                        ));
                event.getTooltipElements().add(index, Either.left(Component.translatable("tooltip.cold_sweat.consumed").withStyle(ChatFormatting.GRAY)));
                event.getTooltipElements().add(index, Either.left(Component.empty()));
                return temp;
            });
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(itemData)) != null && !itemInsul.isEmpty())
        {   event.getTooltipElements().add(tooltipIndex, Either.right(new InsulationTooltip(ConfigSettings.INSULATION_ITEMS.get().get(itemData).split(), InsulationType.ITEM)));
        }
        // If the item is an insulating curio, add the tooltip
        else if (CompatManager.isCuriosLoaded() && (itemInsul = ConfigSettings.INSULATING_CURIOS.get().get(itemData)) != null && !itemInsul.isEmpty())
        {   event.getTooltipElements().add(tooltipIndex, Either.right(new InsulationTooltip(ConfigSettings.INSULATING_CURIOS.get().get(itemData).split(), InsulationType.CURIO)));
        }
        // If the item is insulated armor
        Insulation armorInsul;
        if (stack.getItem() instanceof Wearable && (!Objects.equals((armorInsul = ConfigSettings.INSULATING_ARMORS.get().get(itemData)), itemInsul) || armorInsul == null))
        {
            // Create the list of insulation pairs from NBT
            List<Insulation> insulation = ItemInsulationManager.getInsulationCap(stack)
            .map(c ->
            {
                if (c instanceof ItemInsulationCap cap)
                {   return cap;
                }
                return new ItemInsulationCap();
            }).map(cap -> cap.getInsulationValues().stream().map(Insulation::split).reduce(new ArrayList<>(), (list, insul) ->
            {   list.addAll(insul);
                return list;
            })).orElse(new ArrayList<>());

            // If the armor has intrinsic insulation due to configs, add it to the list
            ConfigSettings.INSULATING_ARMORS.get().computeIfPresent(itemData, (item, pair) ->
            {   insulation.addAll(pair.split());
                return pair;
            });

            // Sort the insulation values from cold to hot
            Insulation.sort(insulation);

            if (!insulation.isEmpty())
            {   event.getTooltipElements().add(tooltipIndex, Either.right(new InsulationTooltip(insulation, InsulationType.ARMOR)));
            }
        }
    }

    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderSoulLampInsertTooltip(ScreenEvent.Render.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP)
            {
                double fuel = screen.getSlotUnderMouse().getItem().getOrCreateTag().getDouble("Fuel");
                ItemStack carriedStack = screen.getMenu().getCarried();

                Double itemFuel;
                if (!carriedStack.isEmpty() && (itemFuel = ConfigSettings.SOULSPRING_LAMP_FUEL.get().get(ItemData.of(carriedStack))) != null)
                {
                    double fuelValue = screen.getMenu().getCarried().getCount() * itemFuel;
                    int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                    int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                    PoseStack ps = event.getPoseStack();
                    if (event.getMouseY() < slotY + 8)
                    {   ps.translate(0, 32, 0);
                    }

                    event.getScreen().renderComponentTooltip(event.getPoseStack(), List.of(Component.literal("       ")), slotX - 18, slotY + 1);

                    RenderSystem.defaultBlendFunc();

                    // Render background
                    RenderSystem.setShaderTexture(0, ClientSoulspringTooltip.TOOLTIP_LOCATION.get());
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 0, 30, 8, 30, 34);

                    // Render ghost overlay
                    RenderSystem.enableBlend();
                    RenderSystem.setShaderColor(1f, 1f, 1f, 0.15f + (float) ((Math.sin(FUEL_FADE_TIMER / 5f) + 1f) / 2f) * 0.4f);
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 8, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 30, 34);
                    RenderSystem.disableBlend();

                    // Render fuel
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
                    GuiComponent.blit(ps, slotX - 7, slotY - 11, 401, 0, 16, (int) (fuel / 2.1333f), 8, 30, 34);
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickSoulLampInsertTooltip(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {   FUEL_FADE_TIMER++;
        }
    }
}
