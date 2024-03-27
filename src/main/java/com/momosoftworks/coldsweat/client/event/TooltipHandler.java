package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.tooltip.ClientSoulspringTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.InsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.SoulspringTooltip;
import com.momosoftworks.coldsweat.common.event.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.configuration.value.ItemValue;
import com.momosoftworks.coldsweat.data.configuration.value.Insulator;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

import java.util.*;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final ChatFormatting COLD = ChatFormatting.BLUE;
    public static final ChatFormatting HOT = ChatFormatting.RED;

    public static void addModifierTooltipLines(List<BaseComponent> tooltip, AttributeModifierMap map)
    {
        map.getMap().asMap().forEach((attribute, modifiers) ->
        {
            for (AttributeModifier.Operation operation : AttributeModifier.Operation.values())
            {
                double value = 0;
                for (AttributeModifier modifier : modifiers.stream().filter(mod -> mod.getOperation() == operation).toList())
                {   value += modifier.getAmount();
                }
                if (value != 0)
                {   tooltip.add(getFormattedAttributeModifier(attribute, value, operation));
                }
            }
        });
    }

    public static MutableComponent getFormattedAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation)
    {
        if (attribute == null) return new TextComponent("");
        double value = amount;
        String attributeName = attribute.getDescriptionId().replace("attribute.", "");

        if (operation == AttributeModifier.Operation.ADDITION
        && (attribute == ModAttributes.FREEZING_POINT
        || attribute == ModAttributes.BURNING_POINT
        || attribute == ModAttributes.WORLD_TEMPERATURE
        || attribute == ModAttributes.BASE_BODY_TEMPERATURE))
        {
            value = Temperature.convertUnits(value, Temperature.Units.MC, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, false);
        }
        String operationString = operation == AttributeModifier.Operation.ADDITION ? "add" : "multiply";
        ChatFormatting color;
        String sign;
        if (value >= 0)
        {
            color = ChatFormatting.BLUE;
            sign = "+";
        }
        else
        {   color = ChatFormatting.RED;
            sign = "";
        }
        String percent;
        if (operation != AttributeModifier.Operation.ADDITION
        || attribute == ModAttributes.HEAT_RESISTANCE
        || attribute == ModAttributes.COLD_RESISTANCE
        || attribute == ModAttributes.HEAT_DAMPENING
        || attribute == ModAttributes.COLD_DAMPENING)
        {   percent = "%";
            value *= 100;
        }
        else
        {   percent = "";
        }
        return new TranslatableComponent(String.format("attribute.cold_sweat.modifier.%s.%s", operationString, attributeName),
                                                       sign + CSMath.formatDoubleOrInt(CSMath.round(value, 2))
                                                       + percent)
                        .withStyle(color);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        var elements = event.getTooltipElements();
        if (stack.isEmpty()) return;

        String hoverName = stack.getHoverName().getString().strip();

        // Get the index at which the tooltip should be inserted
        // Insert the tooltip at the first non-blank line under the item's name
        int tooltipIndex;
        for (tooltipIndex = 0; tooltipIndex < elements.size(); tooltipIndex++)
        {
            if (elements.get(tooltipIndex).left().map(FormattedText::getString).map(String::strip).orElse("").equals(hoverName))
            {
                tooltipIndex++;
                if (elements.size() > tooltipIndex + 1
                && elements.get(tooltipIndex).left().map(FormattedText::getString).orElse("").isBlank()
                && elements.get(tooltipIndex+1).right().isPresent())
                {   tooltipIndex++;
                }
                break;
            }
        }

        Insulator itemInsul = null;
        Player player = Minecraft.getInstance().player;

        // If the item is a Soulspring Lamp
        if (stack.getItem() instanceof SoulspringLampItem)
        {   if (!Screen.hasShiftDown())
            {   elements.add(tooltipIndex, Either.left(new TextComponent("? ").withStyle(ChatFormatting.BLUE).append(new TextComponent("'Shift'").withStyle(ChatFormatting.DARK_GRAY))));
            }
            elements.add(tooltipIndex, Either.right(new SoulspringTooltip(stack.getOrCreateTag().getDouble("Fuel"))));
        }
        // If the item is edible
        else if (stack.getUseAnimation() == UseAnim.DRINK || stack.getUseAnimation() == UseAnim.EAT)
        {
            ConfigSettings.FOOD_TEMPERATURES.get().computeIfPresent(item, (it, temp) ->
            {
                if (!temp.test(stack)) return temp;
                int index = Minecraft.getInstance().options.advancedItemTooltips ? elements.size() - 1 : elements.size();
                elements.add(index, Either.left(
                        temp.value() > 0 ? new TranslatableComponent("tooltip.cold_sweat.temperature_effect", "+" + temp).withStyle(HOT)
                                         : new TranslatableComponent("tooltip.cold_sweat.temperature_effect", temp).withStyle(COLD)
                        ));
                elements.add(index, Either.left(new TranslatableComponent("tooltip.cold_sweat.consumed").withStyle(ChatFormatting.GRAY)));
                elements.add(index, Either.left(new TextComponent("")));
                return temp;
            });
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(item)) != null && !itemInsul.insulation().isEmpty())
        {
            if (itemInsul.predicate().test(player) && itemInsul.nbt().test(stack.getTag()))
            {   elements.add(tooltipIndex, Either.right(new InsulationTooltip(itemInsul.insulation().split(), InsulationSlot.ITEM)));
            }
        }
        // If the item is an insulating curio, add the tooltip
        else if (CompatManager.isCuriosLoaded() && (itemInsul = ConfigSettings.INSULATING_CURIOS.get().get(item)) != null && !itemInsul.insulation().isEmpty())
        {
            if (itemInsul.predicate().test(player) && itemInsul.nbt().test(stack.getTag()))
            {   elements.add(tooltipIndex, Either.right(new InsulationTooltip(itemInsul.insulation().split(), InsulationSlot.CURIO)));
            }
        }
        // If the item is insulated armor
        Insulator armorInsulator = ConfigSettings.INSULATING_ARMORS.get().get(item);
        if (stack.getItem() instanceof Wearable && (!Objects.equals(armorInsulator, itemInsul) || armorInsulator == null))
        {
            // Create the list of insulation pairs from NBT
            List<Insulation> insulation = ItemInsulationManager.getInsulationCap(stack)
                                          // Get insulation values
                                          .map(cap -> cap.getInsulation().stream()
                                          // Filter out insulation that doesn't match the player's predicate
                                          .filter(pair ->
                                          {
                                              ItemStack stack1 = pair.getFirst();
                                              return CSMath.getIfNotNull(ConfigSettings.INSULATION_ITEMS.get().get(stack1.getItem()),
                                                                         insulator -> insulator.predicate().test(player) && insulator.nbt().test(stack1.getTag()),
                                                                         false);
                                          })
                                          // Flat map the insulation values
                                          .map(pair -> pair.getSecond()).reduce(new ArrayList<>(), (list, insul) ->
                                          {
                                              list.addAll(insul);
                                              return list;
                                          }))
                                          .orElse(new ArrayList<>());
            // If the armor has intrinsic insulation due to configs, add it to the list
            if (armorInsulator != null)
            {
                if (armorInsulator.predicate().test(player)
                && armorInsulator.nbt().test(stack.getTag()))
                {
                    insulation.addAll(armorInsulator.insulation().split());
                }
            }

            // Sort the insulation values from cold to hot
            Insulation.sort(insulation);

            if (!insulation.isEmpty())
            {   elements.add(tooltipIndex, Either.right(new InsulationTooltip(insulation, InsulationSlot.ARMOR)));
            }
        }
    }

    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderSoulLampInsertTooltip(ScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP)
            {
                double fuel = screen.getSlotUnderMouse().getItem().getOrCreateTag().getDouble("Fuel");
                ItemStack carriedStack = screen.getMenu().getCarried();

                ItemValue itemFuel;
                if (!carriedStack.isEmpty()
                && (itemFuel = ConfigSettings.SOULSPRING_LAMP_FUEL.get().get(carriedStack.getItem())) != null
                && itemFuel.test(carriedStack))
                {
                    double fuelValue = screen.getMenu().getCarried().getCount() * itemFuel.value();
                    int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                    int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                    PoseStack ps = event.getPoseStack();
                    if (event.getMouseY() < slotY + 8)
                    {   ps.translate(0, 32, 0);
                    }

                    event.getScreen().renderComponentTooltip(event.getPoseStack(), List.of(new TextComponent("       ")), slotX - 18, slotY + 1);

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
