package com.momosoftworks.coldsweat.common.capability.handler;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.item.component.ArmorInsulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.util.item.ItemStackHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class ItemInsulationManager
{
    /**
     * Gets the insulation component from the item, or creates one if needed.<br>
     * This will always return {@code null} for non-armor items!
     */
    public static Optional<ArmorInsulation> getInsulationCap(ItemStack stack)
    {
        if (isInsulatable(stack) && !stack.has(ModItemComponents.ARMOR_INSULATION))
        {   stack.set(ModItemComponents.ARMOR_INSULATION, new ArmorInsulation());
        }
        return Optional.ofNullable(stack.get(ModItemComponents.ARMOR_INSULATION));
    }

    @SubscribeEvent
    public static void handleInventoryOpen(PlayerContainerEvent event)
    {   event.getEntity().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
    }

    public static int getInsulationSlots(ItemStack item)
    {
        Integer[] slots = ConfigSettings.INSULATION_SLOTS.get();
        if (!(item.getItem() instanceof ArmorItem))
        {   return 0;
        }
        return switch (((ArmorItem) item.getItem()).getEquipmentSlot())
        {
            case HEAD  -> slots[0];
            case CHEST -> slots[1];
            case LEGS  -> slots[2];
            case FEET  -> slots[3];
            default -> 0;
        };
    }

    public static boolean isInsulatable(ItemStack stack)
    {
        return stack.getItem() instanceof Equipable
            && !ConfigSettings.INSULATION_ITEMS.get().containsKey(stack.getItem());
    }

    public static List<Insulator> getInsulatorsForStack(ItemStack stack)
    {
        List<Insulator> insulators = new ArrayList<>();
        if (isInsulatable(stack))
        {
            getInsulationCap(stack).ifPresent(cap ->
            {
                for (Pair<ItemStack, List<Insulation>> pair : cap.getInsulation())
                {   CSMath.doIfNotNull(ConfigSettings.INSULATION_ITEMS.get().get(pair.getFirst().getItem()), insulators::add);
                }
            });
        }

        CSMath.doIfNotNull(ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()), insulators::add);
        CSMath.doIfNotNull(ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()), insulators::add);

        return insulators;
    }

    public static List<AttributeModifier> getInsulationAttributeModifiers(ItemStack stack, Attribute attribute, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (Insulator insulator : getInsulatorsForStack(stack))
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

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Attribute attribute, EquipmentSlot slot, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>((operation != null
                                                             ? ItemStackHelper.getAttributeModifiers(stack, slot)
                                                                              .filter(entry -> entry.attribute().equals(Holder.direct(attribute)))
                                                                              .filter(entry -> entry.modifier().operation() == operation)
                                                             : ItemStackHelper.getAttributeModifiers(stack, slot)
                                                                              .filter(entry -> entry.attribute().equals(Holder.direct(attribute))))
                                                             .map(ItemAttributeModifiers.Entry::modifier)
                                                             .toList());
        modifiers.addAll(getInsulationAttributeModifiers(stack, attribute, operation, owner));
        return modifiers;
    }

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Attribute attribute, EquipmentSlot slot)
    {   return getAttributeModifiersForSlot(stack, attribute, slot, null, null);
    }
}
