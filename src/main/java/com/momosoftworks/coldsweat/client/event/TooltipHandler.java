package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import com.momosoftworks.coldsweat.core.init.ModItems;
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
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Supplier;

@EventBusSubscriber(Dist.CLIENT)
public class TooltipHandler
{
    public static final Style COLD = Style.EMPTY.withColor(3767039);
    public static final Style HOT = Style.EMPTY.withColor(16736574);
    public static final Component EXPAND_TOOLTIP_HINT = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
               .append(Component.literal("Shift").withStyle(ChatFormatting.GRAY))
               .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (format) -> {
        format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    private static int HOVERED_ITEM_UPDATE_COOLDOWN = 0;
    private static ItemStack HOVERED_STACK = ItemStack.EMPTY;
    private static int HOVERED_SLOT = 0;
    public static HashMap<UUID, RequirementCheck> HOVERED_STACK_PREDICATES = new HashMap<>();
    public static boolean FETCHING_TOOLTIP = false;
    public static List<Either<FormattedText, TooltipComponent>> LAST_TOOLTIP = new ArrayList<>();
    public static boolean SHOWING_TOOLTIP = false;

    private static final Supplier<LocalPlayer> PLAYER = () -> Minecraft.getInstance().player;

    public static <T extends ConfigData> RequirementCheck checkRequirement(T element)
    {   return HOVERED_STACK_PREDICATES.getOrDefault(element.uuid(), RequirementCheck.UNKNOWN);
    }

    public static boolean isShiftDown()
    {   return Screen.hasShiftDown() || ConfigSettings.EXPAND_TOOLTIPS.get();
    }

    public static int getTooltipTitleIndex(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack)
    {
        if (tooltip.isEmpty()) return 0;

        int tooltipStartIndex;
        String hoverName = stack.getHoverName().getString();

        if (CompatManager.isIcebergLoaded())
        {   tooltipStartIndex = CompatManager.LegendaryTooltips.getTooltipStartIndex(tooltip) + 1;
        }
        else findTitle:
        {
            for (tooltipStartIndex = 0; tooltipStartIndex < tooltip.size(); tooltipStartIndex++)
            {
                if (tooltip.get(tooltipStartIndex).left().map(FormattedText::getString).map(String::strip).orElse("").equals(hoverName))
                {
                    tooltipStartIndex++;
                    break findTitle;
                }
            }
            tooltipStartIndex = 1;
        }
        tooltipStartIndex = CSMath.clamp(tooltipStartIndex, 0, tooltip.size());
        return tooltipStartIndex;
    }

    public static int getTooltipEndIndex(List<Either<FormattedText, TooltipComponent>> tooltip, ItemStack stack)
    {
        int tooltipEndIndex = tooltip.size();
        if (Minecraft.getInstance().options.advancedItemTooltips)
        {
            for (--tooltipEndIndex; tooltipEndIndex > 0; tooltipEndIndex--)
            {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (tooltip.get(tooltipEndIndex).left().map(text -> text.getString().equals(itemId)).orElse(false))
                {   break;
                }
            }
        }
        tooltipEndIndex = CSMath.clamp(tooltipEndIndex, 0, tooltip.size());
        return tooltipEndIndex;
    }

    public static void addModifierTooltipLines(List<Component> tooltip, AttributeModifierMap map, boolean showIcon, boolean strikethrough)
    {
        map.getMap().asMap().forEach((attribute, modifiers) ->
        {
            for (AttributeModifier.Operation operation : AttributeModifier.Operation.values())
            {
                double value = 0;
                for (AttributeModifier modifier : modifiers.stream().filter(mod -> mod.operation() == operation).toList())
                {   value += modifier.amount();
                }
                if (value != 0)
                {   tooltip.add(getFormattedAttributeModifier(attribute, value, operation, showIcon, strikethrough));
                }
            }
        });
    }

    public static MutableComponent getFormattedAttributeModifier(Holder<Attribute> attribute, double value, AttributeModifier.Operation operation,
                                                                 boolean forTooltip, boolean strikethrough)
    {
        if (attribute == null) return Component.empty();
        String attributeID = attribute.value().getDescriptionId().replace("attribute.", "");
        MutableComponent attributeName = Component.translatable(String.format("trait.cold_sweat.%s", attributeID));
        Temperature.Trait trait = EntityTempManager.getTraitForAttribute(attribute);

        /* Compose attribute value text */
        String operationString = operation == AttributeModifier.Operation.ADD_VALUE ? "add" : "multiply";
        // Determine text color and value sign
        boolean isGoodValue = (value >= 0) != trait.isNegativeValueGood();
        ChatFormatting color = isGoodValue ? ChatFormatting.BLUE : ChatFormatting.RED;
        StringBuilder valueTextBuilder = new StringBuilder();
        if (value > 0)
        {   valueTextBuilder.append("+");
        }
        if (operation == AttributeModifier.Operation.ADD_VALUE && trait.isForWorld())
        {   value = Temperature.convertIfNeeded(value, trait, ConfigSettings.UNITS.get(), false);
        }
        if (operation != AttributeModifier.Operation.ADD_VALUE || trait.isProportional())
        {   value *= 100;
        }
        valueTextBuilder.append(CSMath.truncate(value, 1));
        if (operation != AttributeModifier.Operation.ADD_VALUE || trait.isProportional())
        {   valueTextBuilder.append("%");
        }

        /* Compose tooltip component */
        List<Object> params = new ArrayList<>(List.of(valueTextBuilder.toString()));
        MutableComponent component;
        // Create custom component for Cold Sweat attributes
        if (EntityTempManager.isTemperatureAttribute(attribute))
        {
            if (trait == Temperature.Trait.WORLD && operation == AttributeModifier.Operation.ADD_VALUE)
            {   attributeName = attributeName.append(Temperature.Units.C.getFormattedName());
            }
            params.add(1, attributeName);
            component = Component.translatable(String.format("attribute.cold_sweat.modifier.%s", operationString), params.toArray());
        }
        else // Vanilla component; add custom params to it
        {
            component = getFormattedVanillaAttributeModifier(attribute, value, operation);
            TranslatableContents contents = (TranslatableContents) component.getContents();
            params.addAll(0, Arrays.asList(contents.getArgs()));
            component = setComponentContents(component, new TranslatableContents(contents.getKey(), contents.getFallback(), params.toArray()));
        }
        component = component.withStyle(color);
        component = addTooltipFlags(component, forTooltip, strikethrough);

        return component;
    }

    public static MutableComponent getFormattedVanillaAttributeModifier(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation)
    {
        double adjustedAmount = amount;
        if (operation == AttributeModifier.Operation.ADD_VALUE)
        {
            if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE))
            {   adjustedAmount = amount * 10.0D;
            }
            else
            {   adjustedAmount = amount;
            }
        }

        if (amount >= 0.0D)
        {
            return Component.translatable("attribute.modifier.plus." + operation.id(), ATTRIBUTE_MODIFIER_FORMAT.format(adjustedAmount),
                                          Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.BLUE);
        }
        else
        {   adjustedAmount *= -1;
            return Component.translatable("attribute.modifier.take." + operation.id(), ATTRIBUTE_MODIFIER_FORMAT.format(adjustedAmount),
                                            Component.translatable(attribute.value().getDescriptionId())).withStyle(ChatFormatting.RED);
        }
    }

    public static MutableComponent setComponentContents(MutableComponent component, ComponentContents newContents)
    {
        MutableComponent newComponent = MutableComponent.create(newContents).setStyle(component.getStyle());
        component.getSiblings().forEach(newComponent::append);
        return newComponent;
    }

    public static MutableComponent addTooltipFlags(MutableComponent component, boolean showIcon, boolean strikethrough)
    {
        if (component.getContents() instanceof TranslatableContents translatable)
        {
            List<Object> params = new ArrayList<>(Arrays.asList(translatable.getArgs()));
            if (showIcon)
            {   params.add("show_icon");
            }
            if (strikethrough)
            {   params.add("strikethrough");
            }
            return setComponentContents(component, new TranslatableContents(translatable.getKey(), translatable.getFallback(), params.toArray()));
        }
        return component;
    }

    @SubscribeEvent
    public static void updateHoveredItem(RenderTooltipEvent.Pre event)
    {
        if (!SHOWING_TOOLTIP)
        {   LAST_TOOLTIP.clear();
            SHOWING_TOOLTIP = true;
        }
        ItemStack stack = event.getItemStack();

        if (!HOVERED_STACK.equals(stack))
        {
            if (stack.isEmpty())
            {   HOVERED_STACK = stack;
                LAST_TOOLTIP.clear();
                return;
            }
            int slotIndex = 0;
            EquipmentSlot equipmentSlot = null;

            // If open screen is a container, get equipment slot and slot index
            findSlots:
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> menu)
            {
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
                    }
                    PacketDistributor.sendToServer(SyncItemPredicatesMessage.fromClient(slotIndex, equipmentSlot, stack));
                }
            }
        }
        if (FETCHING_TOOLTIP && LAST_TOOLTIP.isEmpty())
        {   event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void tickHoverCooldown(ClientTickEvent.Post event)
    {
        if (HOVERED_ITEM_UPDATE_COOLDOWN > 0)
        {   HOVERED_ITEM_UPDATE_COOLDOWN--;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addCustomTooltips(RenderTooltipEvent.GatherComponents event)
    {
        if (FETCHING_TOOLTIP && !LAST_TOOLTIP.isEmpty())
        {
            event.getTooltipElements().clear();
            event.getTooltipElements().addAll(LAST_TOOLTIP);
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        var elements = event.getTooltipElements();
        InsulationVisibility insulationVisibility = ConfigSettings.INSULATION_VISIBILITY.get();
        Player player = Minecraft.getInstance().player;
        float tickRate = player != null ? player.level().tickRateManager().tickrate() : 20;

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
            {   elements.add(tooltipStartIndex, Either.left(EXPAND_TOOLTIP_HINT));
            }
            elements.add(tooltipStartIndex, Either.right(new SoulspringTooltip(SoulspringLampItem.getFuel(stack))));
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
            int dietTooltipSectionIndex = CSMath.getIndexOf(elements, line -> line.left().map(text -> text.getString().equalsIgnoreCase(Component.translatable("tooltip.diet.eaten").getString())).orElse(false));
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
                MutableComponent consumeEffects = Component.translatable("tooltip.cold_sweat.temperature_effect", tempString, Temperature.Trait.CORE.getFormattedName());
                if (temp > 0)
                {   consumeEffects.setStyle(HOT);
                }
                else if (temp < 0)
                {   consumeEffects.setStyle(COLD);
                }
                // Add a duration to the tooltip if it exists
                if (duration > 0)
                {   consumeEffects.append(" (" + StringUtil.formatTickDuration(duration, tickRate) + ")");
                }
                // Add the effect to the tooltip
                elements.add(index, Either.left(consumeEffects));
            }

            boolean isFood = stack.getUseAnimation() == UseAnim.EAT || stack.getUseAnimation() == UseAnim.DRINK;
            // Don't add our own section title if one already exists
            if (!foodTemps.isEmpty() && (!isFood || dietTooltipSectionIndex == -1))
            {
                MutableComponent sectionTitle = isFood ? Component.translatable("tooltip.cold_sweat.section.consumed")
                                                       : Component.translatable("tooltip.cold_sweat.section.used");
                elements.add(tooltipEndIndex, Either.left(sectionTitle.withStyle(ChatFormatting.GRAY)));
                elements.add(tooltipEndIndex, Either.left(Component.empty()));
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

    private static void addInsulationTooltips(List<Either<FormattedText, TooltipComponent>> elements, int tooltipStartIndex,
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
        {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(armorInsulation, Insulation.Slot.ARMOR, stack, false)));
        }
        if (!unmetArmorInsulation.isEmpty())
        {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(unmetArmorInsulation, Insulation.Slot.ARMOR, stack, true)));
        }

        // Insulation ingredient
        {
            List<InsulatorData> insulation = new ArrayList<>();
            List<InsulatorData> unmetInsulation = new ArrayList<>();
            for (InsulatorData insulator : ConfigSettings.INSULATION_ITEMS.get().get(item))
            {   validateInsulator(insulator, insulation, insulator.hideIfUnmet() ? new ArrayList<>() : unmetInsulation, allUnmetInsulation);
            }
            if (!insulation.isEmpty() && !insulation.stream().map(InsulatorData::insulation).toList().equals(armorInsulation.stream().map(InsulatorData::insulation).toList()))
            {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(insulation, Insulation.Slot.ITEM, stack, false)));
            }
            if (!unmetInsulation.isEmpty() && !unmetInsulation.stream().map(InsulatorData::insulation).toList().equals(unmetArmorInsulation.stream().map(InsulatorData::insulation).toList()))
            {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(unmetInsulation, Insulation.Slot.ITEM, stack, true)));
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
            {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(curioInsulation, Insulation.Slot.CURIO, stack, false)));
            }
            if (!unmetCurioInsulation.isEmpty())
            {   elements.add(tooltipStartIndex, Either.right(new InsulationTooltip(unmetCurioInsulation, Insulation.Slot.CURIO, stack, true)));
            }
        }
    }

    /**
     * Converts attribute modifier lines with insulation icons into InsulationAttributeTooltips and sorts unmet attributes to the bottom of the section
     * @return The index at which unmet modifier hints should be displayed
     */
    private static int convertAndSortUnmetAttributes(List<Either<FormattedText, TooltipComponent>> elements)
    {
        boolean hasAttributes = false;
        boolean foundUnmetAttribute = false;
        int unmetLabelIndex = elements.size();
        int unmetAttributeIndex = elements.size();

        for (int i = 0; i < elements.size(); i++)
        {
            Either<FormattedText, TooltipComponent> element = elements.get(i);
            if (element.left().isPresent() && element.left().get() instanceof Component component)
            {
                if (component.getContents() instanceof TranslatableContents translatableContents
                && translatableContents.getArgs() != null)
                {
                    // Start of new attribute modifiers section
                    if (translatableContents.getKey().contains("item.modifiers"))
                    {
                        if (!hasAttributes)
                        {   hasAttributes = true;
                            unmetLabelIndex = i;
                        }
                        foundUnmetAttribute = false;
                    }
                    List<Object> args = Arrays.asList(translatableContents.getArgs());
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
                            elements.add(unmetAttributeIndex, Either.right(new ConditionalTooltip(component, Minecraft.getInstance().font, strikethrough, Icon.INSULATION.get())));
                            i--;
                        }
                        else elements.set(i, Either.right(new ConditionalTooltip(component, Minecraft.getInstance().font, strikethrough, Icon.INSULATION.get())));
                    }
                }
            }
        }
        return unmetLabelIndex;
    }

    private static void addUnmetRequirementHints(List<Either<FormattedText, TooltipComponent>> elements, int unmetLabelIndex, List<InsulatorData> allUnmetInsulation)
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
                    MutableComponent unmetAttributesTooltip = Component.translatable("tooltip.cold_sweat.unmet_attributes").withStyle(ChatFormatting.RED);
                    elements.add(unmetLabelIndex, Either.right(new ConditionalTooltip(unmetAttributesTooltip, Minecraft.getInstance().font, false, Icon.INSULATION.get())));
                    addedUnmetLabel = true;
                }
                MutableComponent hintText = hint.get().getText();
                if (!hintText.getString().isEmpty())
                {
                    hintText.setStyle(hintText.getStyle().withColor(7561572));
                    elements.add(unmetLabelIndex + hintIndex + 1, Either.right(new ConditionalTooltip(hintText, Minecraft.getInstance().font, true, Icon.INSULATION.get())));
                }
            }
        }
        if (addedUnmetLabel)
        {   elements.add(unmetLabelIndex + hintIndex + 1, Either.left(Component.empty()));
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

    private static void addItemTempsTooltip(List<Either<FormattedText, TooltipComponent>> elements, ItemStack stack, int startIndex)
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
        {   elements.add(index++, Either.left(Component.empty()));
        }
        addItemTempEntries(index, elements, mergedTempMap, mergedUnmetTempMap);
    }

    private static void addItemTempEntries(int index, List<Either<FormattedText, TooltipComponent>> elements,
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
                    MutableComponent sectionTitle = slot.map(bounds -> Component.translatable("tooltip.cold_sweat.section.slot_range", bounds.min(), bounds.max()),
                                                             slotType -> Component.translatable("tooltip.cold_sweat.section.slot_single", slotType.getFormattedName()));
                    elements.add(index, Either.left(sectionTitle.withStyle(ChatFormatting.GRAY)));
                }
                else
                {
                    slots.sort(Comparator.comparing(slot -> slot.map(bounds -> 1, slotType -> 0)));
                    List<String> slotNames = slots.stream().map(either -> either.map(IntegerBounds::toString, ItemTempData.SlotType::getFormattedName)).toList();
                    MutableComponent sectionTitle = Component.translatable("tooltip.cold_sweat.section.slots_list", slotNames.toString());
                    elements.add(index, Either.left(sectionTitle.withStyle(ChatFormatting.GRAY)));
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
                MutableComponent tempText = Component.literal((tempNum > 0 ? "+" : "") + CSMath.formatDoubleOrInt(CSMath.round(tempNum, 2)));

                Style style;
                int sign = Double.compare(tempNum, 0);
                if (trait.isProportional())
                {
                    if (sign < 0 == trait.isNegativeValueGood())
                    {   style = Style.EMPTY.withColor(ChatFormatting.BLUE);
                    }
                    else style = Style.EMPTY.withColor(ChatFormatting.RED);
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
                MutableComponent tooltipText = Component.translatable("tooltip.cold_sweat.temperature_effect", tempText, trait.getFormattedName());
                if (trait == Temperature.Trait.CORE)
                {   tooltipText = Component.translatable("tooltip.cold_sweat.per_second", tooltipText);
                }
                if (unmetTempMap.entrySet().contains(entry))
                {   elements.add(index, Either.right(new ConditionalTooltip(tooltipText, Minecraft.getInstance().font, true, null)));
                }
                else elements.add(index, Either.left(tooltipText.withStyle(style)));
            }
        }
    }

    static int FUEL_FADE_TIMER = 0;

    @SubscribeEvent
    public static void renderSoulLampInsertTooltip(ScreenEvent.Render.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            if (screen.getSlotUnderMouse() != null && screen.getSlotUnderMouse().getItem().getItem() == ModItems.SOULSPRING_LAMP.value())
            {
                double fuel = SoulspringLampItem.getFuel(screen.getSlotUnderMouse().getItem());
                ItemStack carriedStack = screen.getMenu().getCarried();

                FuelData itemFuel = ConfigHelper.getFirstOrNull(ConfigSettings.SOULSPRING_LAMP_FUEL, carriedStack.getItem(), data -> data.test(carriedStack));
                if (!carriedStack.isEmpty()
                && itemFuel != null)
                {
                    double fuelValue = screen.getMenu().getCarried().getCount() * itemFuel.fuel(carriedStack);
                    int slotX = screen.getSlotUnderMouse().x + screen.getGuiLeft();
                    int slotY = screen.getSlotUnderMouse().y + screen.getGuiTop();

                    GuiGraphics graphics = event.getGuiGraphics();
                    PoseStack ps = graphics.pose();
                    ps.pushPose();
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
                    ps.popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public static void tickSoulLampInsertTooltip(ClientTickEvent.Post event)
    {
        FUEL_FADE_TIMER++;
        SHOWING_TOOLTIP = false;
    }
}
