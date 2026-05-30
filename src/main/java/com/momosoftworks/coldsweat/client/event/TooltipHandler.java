package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.tooltip.*;
import com.momosoftworks.coldsweat.client.gui.tooltip.Icon;
import com.momosoftworks.coldsweat.client.gui.tooltip.util.RequirementCheck;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.enums.InsulationVisibility;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.SyncItemPredicatesMessage;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.entity.EntityHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final Style COLD = Style.EMPTY.withColor(Color.fromRgb(3767039));
    public static final Style HOT = Style.EMPTY.withColor(Color.fromRgb(16736574));
    public static final IFormattableTextComponent EXPAND_TOOLTIP_HINT = new StringTextComponent("[").withStyle(TextFormatting.DARK_GRAY)
               .append(new StringTextComponent("Shift").withStyle(TextFormatting.GRAY))
               .append(new StringTextComponent("]").withStyle(TextFormatting.DARK_GRAY));

    private static int HOVERED_ITEM_UPDATE_COOLDOWN = 0;
    private static ItemStack HOVERED_STACK = ItemStack.EMPTY;
    private static int HOVERED_SLOT = 0;
    public static HashMap<UUID, RequirementCheck> HOVERED_STACK_PREDICATES = new HashMap<>();
    public static boolean FETCHING_TOOLTIP = false;
    public static List<ITextComponent> LAST_TOOLTIP = new ArrayList<>();
    public static boolean SHOWING_TOOLTIP = false;

    private static final Supplier<ClientPlayerEntity> PLAYER = () -> Minecraft.getInstance().player;

    public static <T extends ConfigData> RequirementCheck checkRequirement(T element)
    {   return HOVERED_STACK_PREDICATES.getOrDefault(element.uuid(), RequirementCheck.UNKNOWN);
    }

    public static boolean isShiftDown()
    {   return Screen.hasShiftDown() || ConfigSettings.EXPAND_TOOLTIPS.get();
    }

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
        tooltipStartIndex = CSMath.clamp(tooltipStartIndex, 0, tooltip.size());
        return tooltipStartIndex;
    }

    public static int getTooltipEndIndex(List<ITextComponent> tooltip, ItemStack stack)
    {
        int tooltipEndIndex = tooltip.size();
        if (Minecraft.getInstance().options.advancedItemTooltips)
        {
            for (--tooltipEndIndex; tooltipEndIndex > 0; tooltipEndIndex--)
            {
                String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                if (tooltip.get(tooltipEndIndex).getString().equals(itemId))
                {   break;
                }
            }
        }
        tooltipEndIndex = CSMath.clamp(tooltipEndIndex, 0, tooltip.size());
        return tooltipEndIndex;
    }

    public static void addModifierTooltipLines(List<ITextComponent> tooltip, AttributeModifierMap map, boolean showIcon, boolean strikethrough)
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
                {   tooltip.add(getFormattedAttributeModifier(attribute, value, operation, showIcon, strikethrough));
                }
            }
        });
    }

    public static IFormattableTextComponent getFormattedAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation,
                                                                 boolean forTooltip, boolean strikethrough)
    {
        if (attribute == null) return new StringTextComponent("");
        double value = amount;
        String attributeID = attribute.getDescriptionId().replace("attribute.", "");
        IFormattableTextComponent attributeName = new TranslationTextComponent(String.format("trait.cold_sweat.%s", attributeID));
        Temperature.Trait trait = EntityTempManager.getTraitForAttribute(attribute);

        /* Compose attribute value text */
        String operationString = operation == AttributeModifier.Operation.ADDITION ? "add" : "multiply";
        // Determine text color and value sign
        boolean isGoodValue = (value >= 0) != trait.isNegativeValueGood();
        TextFormatting color = isGoodValue ? TextFormatting.BLUE : TextFormatting.RED;
        StringBuilder valueTextBuilder = new StringBuilder();
        if (value > 0)
        {   valueTextBuilder.append("+");
        }
        if (operation == AttributeModifier.Operation.ADDITION && trait.isForWorld())
        {   value = Temperature.convertIfNeeded(value, trait, ConfigSettings.UNITS.get(), false);
        }

        if (operation != AttributeModifier.Operation.ADDITION
                || trait.isProportional())
        {   value *= 100;
        }
        valueTextBuilder.append(CSMath.truncate(value, 1));
        if (operation != AttributeModifier.Operation.ADDITION || trait.isProportional())
        {   valueTextBuilder.append("%");
        }

        /* Compose tooltip component */
        List<Object> params = new ArrayList<>(Arrays.asList(valueTextBuilder.toString()));
        IFormattableTextComponent component;
        // Create custom component for Cold Sweat attributes
        if (EntityTempManager.isTemperatureAttribute(attribute))
        {
            if (trait == Temperature.Trait.WORLD && operation == AttributeModifier.Operation.ADDITION)
            {   attributeName = attributeName.append(Temperature.Units.C.getFormattedName());
            }
            params.add(1, attributeName);
            component = new TranslationTextComponent(String.format("attribute.cold_sweat.modifier.%s", operationString), params.toArray());
        }
        else // Vanilla component; add custom params to it
        {
            component = getFormattedVanillaAttributeModifier(attribute, amount, operation);
            Object[] contents = ((TranslationTextComponent) component).getArgs();
            params.addAll(0, Arrays.asList(contents));
            component = setComponentContents(getFormattedVanillaAttributeModifier(attribute, amount, operation), contents);
        }
        component = component.withStyle(color);
        component = addTooltipFlags(component, forTooltip, strikethrough);

        return component;
    }

    public static IFormattableTextComponent getFormattedVanillaAttributeModifier(Attribute attribute, double amount, AttributeModifier.Operation operation)
    {
        double adjustedAmount;
        if (operation == AttributeModifier.Operation.ADDITION)
        {
            if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE))
            {   adjustedAmount = amount * 10.0D;
            }
            else
            {   adjustedAmount = amount;
            }
        }
        else
        {   adjustedAmount = amount * 100.0D;
        }

        if (amount >= 0.0D)
        {
            return new TranslationTextComponent("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adjustedAmount),
                                          new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.BLUE);
        }
        else
        {   adjustedAmount *= -1;
            return new TranslationTextComponent("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adjustedAmount),
                                            new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.RED);
        }
    }

    public static IFormattableTextComponent setComponentContents(IFormattableTextComponent component, Object[] newContents)
    {
        if (component instanceof TranslationTextComponent)
        {
            TranslationTextComponent translatable = (TranslationTextComponent) component;
            IFormattableTextComponent newComponent = new TranslationTextComponent(translatable.getKey(), newContents).setStyle(component.getStyle());
            component.getSiblings().forEach(newComponent::append);
            return newComponent;
        }
        return component;
    }

    public static IFormattableTextComponent addTooltipFlags(IFormattableTextComponent component, boolean showIcon, boolean strikethrough)
    {
        if (component instanceof TranslationTextComponent)
        {
            TranslationTextComponent translatable = (TranslationTextComponent) component;
            List<Object> params = new ArrayList<>(Arrays.asList(translatable.getArgs()));
            if (showIcon)
            {   params.add("show_icon");
            }
            if (strikethrough)
            {   params.add("strikethrough");
            }
            return setComponentContents(component, params.toArray());
        }
        return component;
    }

    private static void addTooltip(int index, Tooltip tooltip, List<ITextComponent> elements)
    {   elements.add(index, getTooltipComponent(tooltip));
    }

    private static void setTooltip(int index, Tooltip tooltip, List<ITextComponent> elements)
    {   elements.set(index, getTooltipComponent(tooltip));
    }

    private static IFormattableTextComponent getTooltipComponent(Tooltip tooltip)
    {
        FontRenderer font = Minecraft.getInstance().font;
        int spaceWidth = font.width(" ");
        String placeholder = org.apache.commons.lang3.StringUtils.repeat(" ", tooltip.getWidth(font) / spaceWidth);
        return new TranslationTextComponent(placeholder, tooltip);
    }

    @SubscribeEvent
    public static void updateHoveredItem(RenderTooltipEvent.Pre event)
    {
        SHOWING_TOOLTIP = true;
        ItemStack stack = event.getStack();

        if (!HOVERED_STACK.equals(stack))
        {
            if (stack.isEmpty())
            {   HOVERED_STACK = stack;
                LAST_TOOLTIP.clear();
                return;
            }
            int slotIndex = 0;
            EquipmentSlotType equipmentSlot = null;

            // If open screen is a container, get equipment slot and slot index
            findSlots:
            if (Minecraft.getInstance().screen instanceof ContainerScreen<?>)
            {
                ContainerScreen<?> menu = (ContainerScreen<?>) Minecraft.getInstance().screen;
                Slot hoveredSlot = menu.getSlotUnderMouse();
                if (hoveredSlot == null) break findSlots;

                slotIndex = hoveredSlot.getSlotIndex();
                equipmentSlot = EntityHelper.getEquipmentSlot(slotIndex);
            }

            if (HOVERED_ITEM_UPDATE_COOLDOWN <= 0)
            {
                HOVERED_STACK = stack;
                HOVERED_ITEM_UPDATE_COOLDOWN = 5;
                if (slotIndex >= 0 && SyncItemPredicatesMessage.hasDataToSend(stack))
                {
                    if (slotIndex != HOVERED_SLOT)
                    {   FETCHING_TOOLTIP = true;
                        HOVERED_SLOT = slotIndex;
                        ColdSweatPacketHandler.INSTANCE.sendToServer(SyncItemPredicatesMessage.fromClient(slotIndex, equipmentSlot, stack));
                    }
                }
            }
        }
        if (FETCHING_TOOLTIP && LAST_TOOLTIP.isEmpty())
        {   event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickHoverCooldown(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && HOVERED_ITEM_UPDATE_COOLDOWN > 0)
        {   HOVERED_ITEM_UPDATE_COOLDOWN--;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addSimpleTooltips(ItemTooltipEvent event)
    {
        if (FETCHING_TOOLTIP && !LAST_TOOLTIP.isEmpty())
        {
            event.getToolTip().clear();
            event.getToolTip().addAll(LAST_TOOLTIP);
            return;
        }
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        List<ITextComponent> elements = event.getToolTip();
        InsulationVisibility insulationVisibility = ConfigSettings.INSULATION_VISIBILITY.get();
        if (stack.isEmpty()) return;

        // Get the index at which the tooltip should be inserted
        int tooltipStartIndex = getTooltipTitleIndex(elements, stack);
        // Get the index of the end of the tooltip, before the debug info (if enabled)
        int tooltipEndIndex = getTooltipEndIndex(elements, stack);

        /*
         Tooltips for soulspring lamp
         */
        if (stack.getItem() instanceof SoulspringLampItem)
        {
            if (!isShiftDown() && ConfigSettings.ENABLE_HINTS.get())
            {   elements.add(tooltipStartIndex, EXPAND_TOOLTIP_HINT);
            }
            else for (int i = 0; i < CSMath.ceil(ConfigSettings.SOULSPRING_LAMP_FUEL.get().size() / 6d) + 1; i++)
            {   elements.add(tooltipStartIndex, new StringTextComponent(""));
            }
            addTooltip(tooltipStartIndex, new ClientSoulspringTooltip(stack.getOrCreateTag().getDouble("Fuel")), elements);
        }

        /*
         Tooltip for item temperature
         */
        addItemTempsTooltip(elements, stack, tooltipEndIndex);

        /*
         Tooltip for food temperature
         */
        {
            // Check if Diet has their own tooltip already
            int dietTooltipSectionIndex = CSMath.getIndexOf(elements, line -> line.getString().equalsIgnoreCase(new TranslationTextComponent("tooltip.diet.eaten").getString()));
            int index = dietTooltipSectionIndex != -1
                        ? dietTooltipSectionIndex + 1
                        : tooltipEndIndex;

            Map<Integer, Double> foodTemps = new HashMap<>();
            for (FoodData foodData : ConfigSettings.FOOD_TEMPERATURES.get().get(item))
            {
                RequirementCheck check = checkRequirement(foodData);
                if (!check.failed())
                {
                    int duration = foodData.duration(stack, PLAYER.get());
                    double temperature = foodData.temperature(stack, PLAYER.get());
                    if (Math.abs(temperature) >= 0.1)
                    {   foodTemps.merge(duration, temperature, Double::sum);
                    }
                }
            }

            for (Map.Entry<Integer, Double> entry : foodTemps.entrySet())
            {
                double temp = entry.getValue();
                int duration = entry.getKey();

                String clippedTemp = CSMath.formatDoubleOrInt(CSMath.round(temp, 1));
                String tempString = temp >= 0 ? "+" + clippedTemp : clippedTemp;
                IFormattableTextComponent consumeEffects = new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", tempString, Temperature.Trait.CORE.getFormattedName());
                if (temp > 0)
                {   consumeEffects.setStyle(HOT);
                }
                else if (temp < 0)
                {   consumeEffects.setStyle(COLD);
                }
                // Add a duration to the tooltip if it exists
                if (duration > 0)
                {   consumeEffects.append(" (" + StringUtils.formatTickDuration(duration) + ")");
                }
                // Add the effect to the tooltip
                elements.add(index, consumeEffects);
            }

            boolean isFood = stack.getUseAnimation() == UseAction.EAT || stack.getUseAnimation() == UseAction.DRINK;
            // Don't add our own section title if one already exists
            if (!foodTemps.isEmpty() && (!isFood || dietTooltipSectionIndex == -1))
            {
                IFormattableTextComponent sectionTitle = isFood ? new TranslationTextComponent("tooltip.cold_sweat.section.consumed")
                                                                : new TranslationTextComponent("tooltip.cold_sweat.section.used");
                elements.add(tooltipEndIndex, sectionTitle.withStyle(TextFormatting.GRAY));
                elements.add(tooltipEndIndex, new StringTextComponent(""));
            }
        }

        /*
         Tooltips for insulation
         */
        List<InsulatorData> allUnmetInsulation = new ArrayList<>();
        if (insulationVisibility.canShow() && !stack.isEmpty())
        {
            addInsulationTooltips(elements, tooltipStartIndex, stack, item, insulationVisibility, allUnmetInsulation);
        }

        // Custom tooltips for attributes from insulation
        int unmetLabelIndex = convertAndSortUnmetAttributes(elements);

        // Add unmet requirement hints
        if (ConfigSettings.ENABLE_HINTS.get())
        {   addUnmetRequirementHints(elements, unmetLabelIndex, allUnmetInsulation);
        }
        LAST_TOOLTIP.clear();
        LAST_TOOLTIP.addAll(elements);
    }

    private static void addInsulationTooltips(List<ITextComponent> elements, int tooltipStartIndex,
                                              ItemStack stack, Item item, InsulationVisibility insulationVisibility,
                                              List<InsulatorData> allUnmetInsulation)
    {
        // Insulating armor
        List<InsulatorData> armorInsulation = new ArrayList<>();
        List<InsulatorData> unmetArmorInsulation = new ArrayList<>();
        for (InsulatorData insulator : ConfigSettings.INSULATING_ARMORS.get().get(item))
        {   validateInsulator(insulator, armorInsulation, unmetArmorInsulation, allUnmetInsulation);
        }

        ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
        {
            if (cap.getInsulation().isEmpty())
            {   cap.deserializeNBT(stack.getOrCreateTag());
            }

            List<Pair<ItemStack, List<InsulatorData>>> insulatorPairs = cap.getInsulation();

            for (int i = 0; i < insulatorPairs.size(); i++)
            {
                Pair<ItemStack, List<InsulatorData>> pair = insulatorPairs.get(i);
                for (InsulatorData insulator : pair.getSecond())
                {   validateInsulator(insulator, armorInsulation, unmetArmorInsulation, allUnmetInsulation);
                }
            }
        });

        if (!armorInsulation.isEmpty() || insulationVisibility.showsIfEmpty())
        {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(armorInsulation, Insulation.Slot.ARMOR, stack, false), elements);
        }
        if (!unmetArmorInsulation.isEmpty())
        {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(unmetArmorInsulation, Insulation.Slot.ARMOR, stack, true), elements);
        }

        // Insulation ingredient
        {
            List<InsulatorData> insulation = new ArrayList<>();
            List<InsulatorData> unmetInsulation = new ArrayList<>();
            for (InsulatorData insulator : ConfigSettings.INSULATION_ITEMS.get().get(item))
            {   validateInsulator(insulator, insulation, insulator.hideIfUnmet() ? new ArrayList<>() : unmetInsulation, allUnmetInsulation);
            }
            if (!insulation.isEmpty() && !insulation.stream().map(InsulatorData::insulation).collect(Collectors.toList()).equals(armorInsulation.stream().map(InsulatorData::insulation).collect(Collectors.toList())))
            {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(insulation, Insulation.Slot.ITEM, stack, false), elements);
            }
            if (!unmetInsulation.isEmpty() && !unmetInsulation.stream().map(InsulatorData::insulation).collect(Collectors.toList()).equals(unmetArmorInsulation.stream().map(InsulatorData::insulation).collect(Collectors.toList())))
            {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(unmetInsulation, Insulation.Slot.ITEM, stack, true), elements);
            }
        }

        // Insulating curio
        if (CompatManager.isCuriosLoaded())
        {
            List<InsulatorData> curioInsulation = new ArrayList<>();
            List<InsulatorData> unmetCurioInsulation = new ArrayList<>();
            for (InsulatorData insulator : ConfigSettings.INSULATING_CURIOS.get().get(item))
            {   validateInsulator(insulator, curioInsulation, unmetCurioInsulation, allUnmetInsulation);
            }
            if (!curioInsulation.isEmpty())
            {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(curioInsulation, Insulation.Slot.CURIO, stack, false), elements);
            }
            if (!unmetCurioInsulation.isEmpty())
            {   addTooltip(tooltipStartIndex, new ClientInsulationTooltip(unmetCurioInsulation, Insulation.Slot.CURIO, stack, true), elements);
            }
        }
    }

    /**
     * Converts attribute modifier lines with insulation icons into InsulationAttributeTooltips and sorts unmet attributes to the bottom of the section
     * @return The index at which unmet modifier hints should be displayed
     */
    private static int convertAndSortUnmetAttributes(List<ITextComponent> elements)
    {
        boolean hasAttributes = false;
        boolean foundUnmetAttribute = false;
        int unmetLabelIndex = elements.size();
        int unmetAttributeIndex = elements.size();

        for (int i = 0; i < elements.size(); i++)
        {
            ITextComponent element = elements.get(i);
            if (element instanceof TranslationTextComponent)
            {
                TranslationTextComponent component = ((TranslationTextComponent) element);
                if (component.getArgs() != null)
                {
                    // Start of new attribute modifiers section
                    if (component.getKey().contains("item.modifiers"))
                    {
                        if (!hasAttributes)
                        {   hasAttributes = true;
                            unmetLabelIndex = i;
                        }
                        foundUnmetAttribute = false;
                    }
                    List<Object> args = Arrays.asList(component.getArgs());
                    // If the insulation icon should be shown, convert the tooltip into an InsulationAttributeTooltip
                    if (args.contains("show_icon"))
                    {
                        boolean strikethrough = args.contains("strikethrough");
                        // Upon the first unmet attribute, set the index at which unmet attributes start
                        if (strikethrough && !foundUnmetAttribute)
                        {   unmetAttributeIndex = i;
                            foundUnmetAttribute = true;
                        }
                        // Replace the unmet attribute line with a strikethrough InsulationAttributeTooltip and move it to the unmet attributes section
                        if (!strikethrough && i > unmetAttributeIndex)
                        {
                            elements.remove(i);
                            addTooltip(unmetAttributeIndex, new ClientConditionalTooltip(component, Minecraft.getInstance().font, strikethrough, Icon.INSULATION.get()), elements);
                            i--;
                        }
                        else setTooltip(i, new ClientConditionalTooltip(component, Minecraft.getInstance().font, strikethrough, Icon.INSULATION.get()), elements);
                    }
                }
            }
        }
        return unmetLabelIndex;
    }

    private static void addUnmetRequirementHints(List<ITextComponent> elements, int unmetLabelIndex, List<InsulatorData> allUnmetInsulation)
    {
        boolean addedUnmetLabel = false;
        int hintIndex = 0;
        for (; hintIndex < allUnmetInsulation.size(); hintIndex++)
        {
            InsulatorData unmetInsulator = allUnmetInsulation.get(hintIndex);
            Optional<InsulatorData.HintText> hint = unmetInsulator.hint();
            if (hint.isPresent())
            {
                if (!addedUnmetLabel)
                {
                    IFormattableTextComponent unmetAttributesTooltip = new TranslationTextComponent("tooltip.cold_sweat.unmet_attributes").withStyle(TextFormatting.RED);
                    addTooltip(unmetLabelIndex, new ClientConditionalTooltip(unmetAttributesTooltip, Minecraft.getInstance().font, false, Icon.INSULATION.get()), elements);
                    addedUnmetLabel = true;
                }
                IFormattableTextComponent hintText = hint.get().getText();
                if (!hintText.getString().isEmpty())
                {
                    hintText.setStyle(hintText.getStyle().withColor(Color.fromRgb(7561572)));
                    addTooltip(unmetLabelIndex + hintIndex + 1, new ClientConditionalTooltip(hintText, Minecraft.getInstance().font, true, Icon.INSULATION.get()), elements);
                }
            }
        }
        if (addedUnmetLabel)
        {   elements.add(unmetLabelIndex + hintIndex + 1, new StringTextComponent(""));
        }
    }

    private static void validateInsulator(InsulatorData insulator, List<InsulatorData> insulation, List<InsulatorData> unmetInsulation, List<InsulatorData> allUnmetInsulation)
    {
        boolean isEmpty = insulator.insulation().isEmpty();
        RequirementCheck check = checkRequirement(insulator);
        if (check.passed() || check.unknown())
        {   if (!isEmpty) insulation.add(insulator);
        }
        else if (!insulator.hideIfUnmet())
        {   if (!isEmpty) unmetInsulation.add(insulator);
            allUnmetInsulation.add(insulator);
        }
    }

    private static void addItemTempsTooltip(List<ITextComponent> elements, ItemStack stack, int startIndex)
    {
        Map<List<Either<IntegerBounds, ItemTempData.SlotType>>, Map<Temperature.Trait, Double>> tempMap = new HashMap<>();
        Map<List<Either<IntegerBounds, ItemTempData.SlotType>>, Map<Temperature.Trait, Double>> unmetTempMap = new HashMap<>();
        for (ItemTempData tempData : ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem()))
        {
            RequirementCheck check = checkRequirement(tempData);
            if (check.unknown()) continue;
            double temp = tempData.getTemperature(PLAYER.get(), stack);
            Temperature.Trait trait = tempData.trait();
            for (Either<IntegerBounds, ItemTempData.SlotType> slot : tempData.slots())
            {
                List<Either<IntegerBounds, ItemTempData.SlotType>> slotKey = CSMath.arrayList(slot);
                if (check.passed())
                {   tempMap.computeIfAbsent(slotKey, k -> new HashMap<>()).merge(trait, temp, Double::sum);
                }
                else
                {   if (tempData.hideIfUnmet(stack, PLAYER.get())) continue;
                    unmetTempMap.computeIfAbsent(slotKey, k -> new HashMap<>()).merge(trait, temp, Double::sum);
                }
            }
        }
        // Merge entries with same values
        Map<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>> mergedTempMap = new LinkedHashMap<>();
        for (Map.Entry<List<Either<IntegerBounds, ItemTempData.SlotType>>, Map<Temperature.Trait, Double>> entry : tempMap.entrySet())
        {   mergedTempMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).addAll(entry.getKey());
        }
        Map<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>> mergedUnmetTempMap = new LinkedHashMap<>();
        for (Map.Entry<List<Either<IntegerBounds, ItemTempData.SlotType>>, Map<Temperature.Trait, Double>> entry : unmetTempMap.entrySet())
        {   mergedUnmetTempMap.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).addAll(entry.getKey());
        }
        int index = startIndex;
        if (!mergedTempMap.isEmpty())
        {   elements.add(index++, new StringTextComponent(""));
        }
        addItemTempEntries(index, elements, mergedTempMap, mergedUnmetTempMap);
    }

    private static void addItemTempEntries(int index, List<ITextComponent> elements,
                                           Map<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>> tempMap,
                                           Map<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>> unmetTempMap)
    {
        Set<List<Either<IntegerBounds, ItemTempData.SlotType>>> createdSections = new HashSet<>();
        Set<Map.Entry<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>>> mergedEntrySet = CSMath.merge(tempMap.entrySet(), unmetTempMap.entrySet());
        for (Map.Entry<Map<Temperature.Trait, Double>, List<Either<IntegerBounds, ItemTempData.SlotType>>> entry : mergedEntrySet)
        {
            Map<Temperature.Trait, Double> traitTempMap = entry.getKey();
            List<Either<IntegerBounds, ItemTempData.SlotType>> slots = entry.getValue();
            if (!createdSections.contains(slots))
            {
                if (slots.size() == 1)
                {
                    Either<IntegerBounds, ItemTempData.SlotType> slot = slots.get(0);
                    IFormattableTextComponent sectionTitle = slot.map(bounds -> new TranslationTextComponent("tooltip.cold_sweat.section.slot_range", bounds.min(), bounds.max()),
                                                                  slotType -> new TranslationTextComponent("tooltip.cold_sweat.section.slot_single", slotType.getFormattedName()));
                    elements.add(index, sectionTitle.withStyle(TextFormatting.GRAY));
                }
                else
                {
                    slots.sort(Comparator.comparing(slot -> slot.map(bounds -> 1, slotType -> 0)));
                    List<String> slotNames = slots.stream().map(either -> either.map(IntegerBounds::toString, ItemTempData.SlotType::getFormattedName)).collect(Collectors.toList());
                    IFormattableTextComponent sectionTitle = new TranslationTextComponent("tooltip.cold_sweat.section.slots_list", slotNames);
                    elements.add(index, sectionTitle.withStyle(TextFormatting.GRAY));
                }
            }
            createdSections.add(slots);
            index++;
            for (Map.Entry<Temperature.Trait, Double> tempEntry : traitTempMap.entrySet())
            {
                Temperature.Trait trait = tempEntry.getKey();
                double effect = tempEntry.getValue();
                if (trait == Temperature.Trait.CORE)
                {   effect *= 20;
                }
                double tempNum = trait.isForWorld() ? Temperature.convert(effect, Temperature.Units.MC, ConfigSettings.UNITS.get(), false) : effect;
                IFormattableTextComponent tempText = new StringTextComponent((tempNum > 0 ? "+" : "") + CSMath.formatDoubleOrInt(CSMath.round(tempNum, 2)));

                Style style;
                int sign = Double.compare(tempNum, 0);
                if (trait.isProportional())
                {
                    if (sign < 0 == trait.isNegativeValueGood())
                    {   style = Style.EMPTY.withColor(TextFormatting.BLUE);
                    }
                    else style = Style.EMPTY.withColor(TextFormatting.RED);
                }
                else
                {
                    if (sign > 0)
                    {   style = HOT;
                    }
                    else if (sign < 0)
                    {   style = COLD;
                    }
                    else style = Style.EMPTY;
                }
                IFormattableTextComponent tooltipText = new TranslationTextComponent("tooltip.cold_sweat.temperature_effect", tempText, trait.getFormattedName());
                if (trait == Temperature.Trait.CORE)
                {   tooltipText = new TranslationTextComponent("tooltip.cold_sweat.per_second", tooltipText);
                }
                if (unmetTempMap.entrySet().contains(entry))
                {   addTooltip(index, new ClientConditionalTooltip(tooltipText, Minecraft.getInstance().font, true, null), elements);
                }
                else elements.add(index, tooltipText.withStyle(style));
            }
        }
    }

    @SubscribeEvent
    public static void renderTooltips(RenderTooltipEvent.PostText event)
    {
        FontRenderer font = Minecraft.getInstance().font;

        // Find the empty line that this tooltip should fill
        int y = event.getY() + 1;
        List<? extends ITextProperties> tooltipLines = event.getLines();
        for (int i = 0; i < tooltipLines.size(); i++)
        {
            ITextProperties tooltipLine = tooltipLines.get(i);
            if (tooltipLine instanceof TranslationTextComponent && ((TranslationTextComponent) tooltipLine).getArgs().length > 0)
            {
                Object arg = ((TranslationTextComponent) tooltipLine).getArgs()[0];
                if (arg instanceof Tooltip)
                {
                    Tooltip tooltip = (Tooltip) arg;
                    tooltip.renderImage(font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 400);
                    tooltip.renderText(font, event.getX(), y, event.getMatrixStack(), Minecraft.getInstance().getItemRenderer(), 400);
                }
            }
            y += font.lineHeight + 1;
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

                FuelData itemFuel = ConfigHelper.getFirstOrNull(ConfigSettings.SOULSPRING_LAMP_FUEL, carriedStack.getItem(), data -> data.test(carriedStack));
                if (!carriedStack.isEmpty()
                && itemFuel != null)
                {
                    double fuelValue = carriedStack.getCount() * itemFuel.fuel(carriedStack);
                    int slotX = inventoryScreen.getSlotUnderMouse().x + ((ContainerScreen<?>) event.getGui()).getGuiLeft();
                    int slotY = inventoryScreen.getSlotUnderMouse().y + ((ContainerScreen<?>) event.getGui()).getGuiTop();

                    MatrixStack ms = event.getMatrixStack();
                    ms.pushPose();
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
                    ms.popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickSoulLampInsertTooltip(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {   FUEL_FADE_TIMER++;
            SHOWING_TOOLTIP = false;
        }
        else if (!SHOWING_TOOLTIP)
        {   LAST_TOOLTIP.clear();
        }
    }
}
