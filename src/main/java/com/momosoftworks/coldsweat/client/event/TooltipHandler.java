package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.tooltip.ClientInsulationTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.ClientSoulspringTooltip;
import com.momosoftworks.coldsweat.client.gui.tooltip.Tooltip;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.config.type.PredicateItem;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.exceptions.RegistryFailureException;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModAttributes;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final TextFormatting COLD = TextFormatting.BLUE;
    public static final TextFormatting HOT  = TextFormatting.RED;

    private static int TOOLTIP_BACKGROUND_COLOR = 0;

    public static int getTooltipTitleIndex(List<ITextComponent> tooltip, ItemStack stack)
    {
        if (tooltip.isEmpty()) return 0;

        int tooltipStartIndex;
        String hoverName = stack.getHoverName().getString();

        for (tooltipStartIndex = 0; tooltipStartIndex < tooltip.size(); tooltipStartIndex++)
        {
            if (tooltip.get(tooltipStartIndex).getString().trim().equals(hoverName))
            {   tooltipStartIndex++;
                break;
            }
        }
        if (tooltipStartIndex == tooltip.size())
        {   tooltipStartIndex = 1;
        }
        return tooltipStartIndex;
    }

    public static int getTooltipEndIndex(List<ITextComponent> tooltip, ItemStack stack)
    {
        if (tooltip.isEmpty()) return 0;

        int tooltipEndIndex = tooltip.size();
        if (Minecraft.getInstance().options.advancedItemTooltips)
        {
            while (tooltip.get(tooltipEndIndex - 1).getString().equals(stack.getItem().getRegistryName().toString()))
            {   tooltipEndIndex--;
            }
            tooltipEndIndex--;
        }
        if (tooltipEndIndex == -1)
        {   tooltipEndIndex = tooltip.size();
        }
        return tooltipEndIndex;
    }

    public static void addModifierTooltipLines(List<ITextComponent> tooltip, AttributeModifierMap map)
    {
        map.getMap().asMap().forEach((attribute, modifiers) ->
        {
            for (AttributeModifier.Operation operation : AttributeModifier.Operation.values())
            {
                double value = 0;
                for (AttributeModifier modifier : modifiers.stream().filter(mod -> mod.getOperation() == operation).collect(Collectors.toList()))
                {   value += modifier.getAmount();
                }
                if (value != 0)
                {   tooltip.add(getFormattedAttributeModifier(attribute, value, operation));
                }
            }
        });
    }

    public static IFormattableTextComponent getFormattedAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation)
    {
        if (attribute == null) return new StringTextComponent("");
        double value = amount;
        String attributeName = attribute.getDescriptionId().replace("attribute.", "");

        if (operation == AttributeModifier.Operation.ADDITION
                && (attribute == ModAttributes.FREEZING_POINT
                || attribute == ModAttributes.BURNING_POINT
                || attribute == ModAttributes.WORLD_TEMPERATURE
                || attribute == ModAttributes.BASE_BODY_TEMPERATURE))
        {
            value = Temperature.convert(value, Temperature.Units.MC, ConfigSettings.CELSIUS.get() ? Temperature.Units.C : Temperature.Units.F, false);
        }
        String operationString = operation == AttributeModifier.Operation.ADDITION ? "add" : "multiply";
        TextFormatting color;
        String sign;
        if (value >= 0)
        {
            color = TextFormatting.BLUE;
            sign = "+";
        }
        else
        {   color = TextFormatting.RED;
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
        return new TranslationTextComponent(String.format("attribute.cold_sweat.modifier.%s.%s", operationString, attributeName),
                                         sign + CSMath.formatDoubleOrInt(CSMath.round(value, 2))
                                                 + percent)
                .withStyle(color);
    }

    @SubscribeEvent
    public static void trackTooltipColor(RenderTooltipEvent.Color event)
    {   TOOLTIP_BACKGROUND_COLOR = event.getBackground();
    }

    @SubscribeEvent
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<ITextComponent> elements = event.getToolTip();
        if (stack.isEmpty()) return;

        // Get the index at which the tooltip should be inserted
        int tooltipStartIndex = getTooltipTitleIndex(elements, stack);
        // Get the index of the end of the tooltip, before the debug info (if enabled)
        int tooltipEndIndex = getTooltipEndIndex(elements, stack);

        Insulator itemInsul = null;
        PlayerEntity player = Minecraft.getInstance().player;
        if (stack.getItem() == ModItems.SOULSPRING_LAMP)
        {
            if (!Screen.hasShiftDown())
            {   elements.add(tooltipStartIndex, new StringTextComponent("? ").withStyle(TextFormatting.BLUE).append(new StringTextComponent("'Shift'").withStyle(TextFormatting.DARK_GRAY)));
            }
            else for (int i = 0; i < CSMath.ceil(ConfigSettings.SOULSPRING_LAMP_FUEL.get().size() / 6d) + 1; i++)
            {   elements.add(tooltipStartIndex, new StringTextComponent(""));
            }
            elements.add(tooltipStartIndex, getTooltipCode(ClientSoulspringTooltip.class));
        }
        else if (stack.getUseAnimation() == UseAction.DRINK || stack.getUseAnimation() == UseAction.EAT)
        {
            PredicateItem temp = ConfigSettings.FOOD_TEMPERATURES.get().get(stack.getItem());
            if (temp != null && temp.test(player, stack))
            {
                elements.add(tooltipEndIndex, temp.value > 0
                        ? new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", "+" + CSMath.formatDoubleOrInt(temp.value)).withStyle(HOT)
                        : new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", CSMath.formatDoubleOrInt(temp.value)).withStyle(COLD)
                );
                elements.add(tooltipEndIndex, new TranslationTextComponent("tooltip.cold_sweat.consumed").withStyle(TextFormatting.GRAY));
                elements.add(tooltipEndIndex, new StringTextComponent(""));
            }
        }
        // Is insulation item
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().getOrDefault(item,
                              ConfigSettings.INSULATING_ARMORS.get().getOrDefault(item,
                              ConfigSettings.INSULATING_CURIOS.get().get(item)))) != null
        && !itemInsul.insulation.isEmpty())
        {
            if (itemInsul.test(player, stack))
            {   elements.add(tooltipStartIndex, getTooltipCode(ClientInsulationTooltip.class));
            }
        }
        // Has insulation (armor)
        else if (stack.getItem() instanceof IArmorVanishable)
        {
            LazyOptional<IInsulatableCap> iCap = ItemInsulationManager.getInsulationCap(stack);
            if (iCap.isPresent())
            {
                IInsulatableCap cap = iCap.orElseThrow(NullPointerException::new);
                if (cap.getInsulation().stream().anyMatch(ins -> ConfigSettings.INSULATION_ITEMS.get().get(ins.getFirst().getItem()).test(player, stack)))
                {   elements.add(tooltipStartIndex, getTooltipCode(ClientInsulationTooltip.class));
                }
            }
        }
    }

    @SubscribeEvent
    public static void renderCustomTooltips(RenderTooltipEvent.PostText event)
    {
        if (Minecraft.getInstance().player != null && !Minecraft.getInstance().player.inventory.getCarried().isEmpty()) return;
        ItemStack stack = event.getStack();
        Item item = stack.getItem();
        if (stack.isEmpty()) return;

        AtomicReference<Tooltip> tooltip = new AtomicReference<>();
        PlayerEntity player = Minecraft.getInstance().player;

        Insulator itemInsul = null;

        if (stack.getItem() instanceof SoulspringLampItem)
        {   tooltip.set(new ClientSoulspringTooltip(stack.getOrCreateTag().getDouble("Fuel")));
        }
        // If the item is an insulation ingredient, add the tooltip
        else if ((itemInsul = ConfigSettings.INSULATION_ITEMS.get().get(item)) != null && !itemInsul.insulation.isEmpty())
        {
            if (itemInsul.test(player, stack))
            {   tooltip.set(new ClientInsulationTooltip(itemInsul.insulation.split(), Insulation.Slot.ITEM, stack));
            }
        }
        // If the item is an insulating curio, add the tooltip
        else if (CompatManager.isCuriosLoaded() && (itemInsul = ConfigSettings.INSULATING_CURIOS.get().get(item)) != null && !itemInsul.insulation.isEmpty())
        {
            if (itemInsul.test(player, stack))
            {   tooltip.set(new ClientInsulationTooltip(itemInsul.insulation.split(), Insulation.Slot.CURIO, stack));
            }
        }

        // If the item is insulated armor
        Insulator armorInsulator = ConfigSettings.INSULATING_ARMORS.get().get(item);
        if (stack.getItem() instanceof IArmorVanishable && (!Objects.equals(armorInsulator, itemInsul) || armorInsulator == null))
        {
            ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
            {
                cap.deserializeNBT(stack.getOrCreateTag());
                // Create the list of insulation pairs from NBT
                List<Insulation> insulation = new ArrayList<>(cap.getInsulation().stream()
                                              // Filter out insulation that doesn't match the player's predicate
                                              .filter(pair ->
                                              {
                                                  ItemStack stack1 = pair.getFirst();
                                                  return CSMath.getIfNotNull(ConfigSettings.INSULATION_ITEMS.get().get(stack1.getItem()),
                                                                             insulator -> insulator.test(player, stack),
                                                                             true);
                                              })
                                              .map(Pair::getSecond).flatMap(List::stream).collect(Collectors.toList()));
                // If the armor has intrinsic insulation due to configs, add it to the list
                if (armorInsulator != null)
                {
                    if (armorInsulator.test(player, stack))
                    {   insulation.addAll(armorInsulator.insulation.split());
                    }
                }

                if (!insulation.isEmpty())
                {   tooltip.set(new ClientInsulationTooltip(insulation, Insulation.Slot.ARMOR, stack));
                }
            });
        }
        // Find the empty line that this tooltip should fill
        if (tooltip.get() != null)
        {
            String lineToReplace = TOOLTIPS.get(tooltip.get().getClass());

            int y = event.getY() - 10;
            if (lineToReplace != null)
            {
                List<? extends ITextProperties> tooltipLines = event.getLines();
                for (ITextProperties tooltipLine : tooltipLines)
                {
                    if (lineToReplace.equals(tooltipLine.getString()))
                    {   break;
                    }
                    y += 10;
                }
                tooltip.get().renderImage(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
                tooltip.get().renderText(Minecraft.getInstance().font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 0);
            }
        }
    }

    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderSoulLampInsertTooltip(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if (event.getGui() instanceof ContainerScreen)
        {
            ContainerScreen<?> inventoryScreen = (ContainerScreen<?>) event.getGui();
            PlayerEntity player = Minecraft.getInstance().player;

            if (player != null && inventoryScreen.getSlotUnderMouse() != null
            && inventoryScreen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP)
            {
                double fuel = inventoryScreen.getSlotUnderMouse().getItem().getOrCreateTag().getDouble("Fuel");
                ItemStack carriedStack = player.inventory.getCarried();

                PredicateItem itemFuel;
                if (!carriedStack.isEmpty()
                && (itemFuel = ConfigSettings.SOULSPRING_LAMP_FUEL.get().get(carriedStack.getItem())) != null
                && itemFuel.test(carriedStack))
                {
                    double fuelValue = carriedStack.getCount() * itemFuel.value;
                    int slotX = inventoryScreen.getSlotUnderMouse().x + ((ContainerScreen<?>) event.getGui()).getGuiLeft();
                    int slotY = inventoryScreen.getSlotUnderMouse().y + ((ContainerScreen<?>) event.getGui()).getGuiTop();

                    MatrixStack ms = event.getMatrixStack();

                    // If the mouse is above the slot, move the box to the bottom
                    if (event.getMouseY() < slotY + 8)
                        ms.translate(0, 32, 0);

                    event.getGui().renderTooltip(ms, new StringTextComponent("       "), slotX - 18, slotY);

                    RenderSystem.defaultBlendFunc();

                    // Render background
                    Minecraft.getInstance().textureManager.bind(ClientSoulspringTooltip.TOOLTIP_LOCATION.get());
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 0, 30, 8, 34, 30);

                    // Render ghost overlay
                    RenderSystem.enableBlend();
                    RenderSystem.color4f(1f, 1f, 1f, 0.15f + (float) ((Math.sin(FUEL_FADE_TIMER / 5f) + 1f) / 2f) * 0.4f);
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 8, Math.min(30, (int) ((fuel + fuelValue) / 2.1333f)), 8, 34, 30);
                    RenderSystem.disableBlend();

                    // Render current fuel
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1f);
                    AbstractGui.blit(ms, slotX - 7, slotY - 12, 401, 0, 16, (int) (fuel / 2.1333f), 8, 34, 30);
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

    private static final Map<Class<? extends Tooltip>, String> TOOLTIPS = new HashMap<>();
    private static int TOOLTIP_REGISTRY_SIZE = 0;

    private static void registerTooltip(Class<? extends Tooltip> tooltip)
    {
        if (!TOOLTIPS.containsKey(tooltip))
        {
            if (TOOLTIP_REGISTRY_SIZE >= 63)
            {   throw new RuntimeException("Too many tooltips registered!");
            }
            String code = Integer.toBinaryString(TOOLTIP_REGISTRY_SIZE);
            while (code.length() < 5)
            {   code = "0" + code;
            }
            code = code.replace("0", "-");
            code = code.replace("1", "+");
            TOOLTIPS.put(tooltip, code);
            TOOLTIP_REGISTRY_SIZE++;
        }
        else
        {
            throw new RegistryFailureException(tooltip, "Tooltips", "Tooltip already registered!", null);
        }
    }

    private static ITextComponent getTooltipCode(Class<? extends Tooltip> tooltip)
    {
        return new StringTextComponent(TOOLTIPS.get(tooltip)).withStyle(Style.EMPTY.withColor(Color.fromRgb(TOOLTIP_BACKGROUND_COLOR)));
    }

    static
    {
        registerTooltip(ClientInsulationTooltip.class);
        registerTooltip(ClientSoulspringTooltip.class);
    }
}
