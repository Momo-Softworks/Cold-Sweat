package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
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

public class ItemInsulationManager
{
    @EventBusSubscriber
    public static class Events
    {
        @SubscribeEvent
        public static void handleInventoryOpen(PlayerContainerEvent event)
        {   event.getEntity().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
        }
    }

    public static Optional<ItemInsulationCap> getInsulationCap(ItemStack stack)
    {
        if (isInsulatable(stack) && !stack.has(ModItemComponents.ARMOR_INSULATION))
        {   stack.set(ModItemComponents.ARMOR_INSULATION, new ItemInsulationCap());
        }
        return Optional.ofNullable(stack.get(ModItemComponents.ARMOR_INSULATION));
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
            && getInsulatorInsulation(stack).isEmpty();
    }

    public static int getSlotsFilled(Collection<InsulatorData> insulators)
    {
        int slots = 0;
        for (InsulatorData data : insulators)
        {
            if (data.fillSlots())
            {   slots += Insulation.splitList(data.insulation()).size();
            }
            else slots++;
        }
        return slots;
    }

    public static Multimap<Item, InsulatorData> getInsulatorsForSlotType(Insulation.Slot slot)
    {
        return switch (slot)
        {
            case ITEM -> ConfigSettings.INSULATION_ITEMS.get();
            case ARMOR -> ConfigSettings.INSULATING_ARMORS.get();
            case CURIO -> ConfigSettings.INSULATING_CURIOS.get();
        };
    }

    /**
     * Gives a collection of all insulation that the item can grant to armor.
     * @return an IMMUTABLE list of insulation the item has.
     */
    public static List<Insulation> getInsulatorInsulation(ItemStack stack)
    {
        if (!ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem()))
        {   return ImmutableList.of();
        }

        return ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()).stream()
               .map(InsulatorData::insulation)
               .flatMap(List::stream)
               .filter(ins -> !ins.isEmpty()).toList();
    }

    /**
     * Returns a list of {@link InsulatorData} attached to the item, including both built-in and applied insulation.<br>
     * Use {@link com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder#filterValid(List, ItemStack)} to restrict the results to only active insulators.
     * @return an IMMUTABLE list of insulation the item has.
     */
    public static List<InsulatorData> getInsulatorsForStack(ItemStack stack, Insulation.Slot slot)
    {
        if (stack.isEmpty()) return new ArrayList<>();

        List<InsulatorData> insulators = new ArrayList<>();
        // Get applied armor insulation
        if (slot == Insulation.Slot.ARMOR && isInsulatable(stack))
        {
            getInsulationCap(stack).ifPresent(cap ->
            {   insulators.addAll(getAppliedArmorInsulators(stack));
            });
        }
        insulators.addAll(getInsulatorsForSlotType(slot).get(stack.getItem()));

        return insulators;
    }

    /**
     * Returns a list of all valid insulation applied to the given armor item.<br>
     * Insulation is considered valid if its requirement passes for the given armor and entity.
     * @param armor The armor item from which to get insulation.
     * @return an IMMUTABLE list of valid insulation on the armor item
     */
    public static List<InsulatorData> getAppliedArmorInsulators(ItemStack armor)
    {
        return ItemInsulationManager.getInsulationCap(armor)
               .map(ItemInsulationCap::getInsulation).orElse(new ArrayList<>())
               .stream()
               .map(Pair::getSecond)
               .flatMap(Collection::stream).toList();
    }

    /**
     * Gathers a list of modifiers for the given attribute that are on this stack, from its insulation
     * @param operation Optional. Filters the output to only include modifiers with the given operation.
     * @param owner Optional. The entity wearing the item. This will be used to check the validity of the insulation before its modifiers are added to the list.
     */
    public static List<AttributeModifier> getArmorInsulationAttributes(ItemStack stack, Holder<Attribute> attribute, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (InsulatorData insulator : getInsulatorsForStack(stack, Insulation.Slot.ARMOR))
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
        modifiers.addAll(getArmorInsulationAttributes(stack, attribute, operation, owner));
        return modifiers;
    }

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Holder<Attribute> attribute, EquipmentSlot slot)
    {   return getAttributeModifiersForSlot(stack, attribute, slot, null, null);
    }
}
