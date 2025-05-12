package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemInsulationSlotsData;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@EventBusSubscriber
public class ItemInsulationManager
{
    /**
     * Gets the insulation component from the item, or creates one if needed.<br>
     * This will always return {@code null} for non-armor items!
     */
    public static Optional<ItemInsulationCap> getInsulationCap(ItemStack stack)
    {
        if (isInsulatable(stack) && !stack.has(ModItemComponents.ARMOR_INSULATION))
        {   stack.set(ModItemComponents.ARMOR_INSULATION, new ItemInsulationCap());
        }
        return Optional.ofNullable(stack.get(ModItemComponents.ARMOR_INSULATION));
    }

    @SubscribeEvent
    public static void handleInventoryOpen(PlayerContainerEvent event)
    {   event.getEntity().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
    }

    /**
     * @return The number of insulation slots on this armor item, or 0 if it does not support insulation
     */
    public static int getInsulationSlots(ItemStack stack)
    {
        if (isInsulatable(stack))
        {
            Item item = stack.getItem();
            Optional<ItemInsulationSlotsData> slotOverride = ConfigSettings.INSULATION_SLOT_OVERRIDES.get().get(item).stream().findFirst();
            if (slotOverride.isPresent() && slotOverride.get().test(stack))
            {   return slotOverride.get().slots();
            }
            else return ConfigSettings.INSULATION_SLOTS.get().getSlots(((Equipable) item).getEquipmentSlot(), stack);
        }
        else return 0;
    }

    /**
     * Checks if the item is allowed to receive insulation from sewing.<br>
     * The item must be equipable and not have any built-in insulation.
     */
    public static boolean isInsulatable(ItemStack stack)
    {
        return stack.getItem() instanceof Equipable
            && !ConfigSettings.INSULATION_BLACKLIST.get().contains(stack.getItem())
            && getBuiltinInsulation(stack).isEmpty();
    }

    /**
     * Gives a collection of all insulation that is built-in to the item (not applied via sewing)
     * @return an IMMUTABLE list of insulation the item has.
     */
    public static List<Insulation> getBuiltinInsulation(ItemStack stack)
    {
        return Stream.of(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()),
                         ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()),
                         ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()))
               .flatMap(Collection::stream).map(InsulatorData::insulation)
               .flatMap(List::stream)
               .filter(ins -> !ins.isEmpty()).toList();
    }

    /**
     * Returns a list of {@link InsulatorData} attached to the item, including both built-in and applied insulation.
     * @return an IMMUTABLE list of insulation the item has.
     */
    public static List<InsulatorData> getAllInsulatorsForStack(ItemStack stack)
    {
        if (stack.isEmpty()) return new ArrayList<>();

        List<InsulatorData> insulators = new ArrayList<>();
        if (isInsulatable(stack))
        {
            getInsulationCap(stack).ifPresent(cap ->
            {
                for (Pair<ItemStack, List<InsulatorData>> pair : cap.getInsulation())
                {   insulators.addAll(ConfigSettings.INSULATION_ITEMS.get().get(pair.getFirst().getItem()));
                }
            });
        }
        insulators.addAll(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()));
        insulators.addAll(ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()));
        insulators.addAll(ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()));

        return insulators;
    }

    /**
     * Returns a list of all valid insulation applied to the given armor item.<br>
     * Insulation is considered valid if its requirement passes for the given armor and entity.
     * @param armor The armor item from which to get insulation.
     * @param entity The entity wearing the item. If null, the insulators' entity requirements will always pass.
     * @return an IMMUTABLE list of valid insulation on the armor item
     */
    public static List<InsulatorData> getEffectiveAppliedInsulation(ItemStack armor, @Nullable LivingEntity entity)
    {
        return ItemInsulationManager.getInsulationCap(armor)
               .map(ItemInsulationCap::getInsulation).orElse(new ArrayList<>())
               .stream()
               .map(pair -> pair.mapSecond(insulators -> insulators.stream().filter(entry -> entry.test(entity, pair.getFirst())).toList()))
               .map(Pair::getSecond).flatMap(Collection::stream).toList();
    }

    /**
     * Gets both applied an intrinsic insulation on the armor item.<br>
     * See {@link #getEffectiveAppliedInsulation(ItemStack, LivingEntity)} for more information.
     */
    public static List<InsulatorData> getAllEffectiveInsulation(ItemStack armor, @Nullable LivingEntity entity)
    {
        List<InsulatorData> insulation = new ArrayList<>(getEffectiveAppliedInsulation(armor, entity));
        insulation.addAll(ConfigSettings.INSULATING_ARMORS.get().get(armor.getItem()).stream().filter(insulator -> insulator.test(entity, armor)).toList());
        return ImmutableList.copyOf(insulation);
    }

    /**
     * Gathers a list of modifiers for the given attribute that are on this stack, from its insulation
     * @param operation Optional. Filters the output to only include modifiers with the given operation.
     * @param owner Optional. The entity wearing the item. This will be used to check the validity of the insulation before its modifiers are added to the list.
     */
    public static List<AttributeModifier> getAppliedInsulationAttributes(ItemStack stack, Holder<Attribute> attribute, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (InsulatorData insulator : getAllInsulatorsForStack(stack))
        {
            if (insulator.test(owner, stack))
            {
                modifiers.addAll(insulator.attributes().get(attribute)
                                          .stream()
                                          .filter(mod -> operation == null || mod.operation() == operation)
                                          .toList());
            }
        }
        return modifiers;
    }

    /**
     * Gets all attribute modifiers for the given attribute that this item has, provided it is in the given slot. This includes modifiers from the item itself and from its insulation.
     */
    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Holder<Attribute> attribute, EquipmentSlot slot, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>((operation != null
                                                             ? ItemStackHelper.getAttributeModifiers(stack, slot)
                                                                              .filter(entry -> entry.attribute().equals(Holder.direct(attribute)))
                                                                              .filter(entry -> entry.modifier().operation() == operation)
                                                             : ItemStackHelper.getAttributeModifiers(stack, slot)
                                                                              .filter(entry -> entry.attribute().equals(Holder.direct(attribute))))
                                                             .map(ItemAttributeModifiers.Entry::modifier)
                                                             .toList());
        modifiers.addAll(getAppliedInsulationAttributes(stack, attribute, operation, owner));
        return modifiers;
    }

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Holder<Attribute> attribute, EquipmentSlot slot)
    {   return getAttributeModifiersForSlot(stack, attribute, slot, null, null);
    }
}
