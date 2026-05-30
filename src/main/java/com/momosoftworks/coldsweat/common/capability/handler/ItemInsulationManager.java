package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.SidedCapabilityCache;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemInsulationSlotsData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ItemInsulationManager
{
    public static SidedCapabilityCache<IInsulatableCap, ItemStack> CAP_CACHE = new SidedCapabilityCache<>(ModCapabilities.ITEM_INSULATION);

    @Mod.EventBusSubscriber
    public static class Events
    {
        @SubscribeEvent
        public static void attachCapabilityToItemHandler(AttachCapabilitiesEvent<ItemStack> event)
        {
            ItemStack stack = event.getObject();
            if (isInsulatable(stack))
            {
                // Make a new capability instance to attach to the item
                ItemInsulationCap itemInsulationCap = new ItemInsulationCap();
                // Optional that holds the capability instance
                final LazyOptional<IInsulatableCap> capOptional = LazyOptional.of(() -> itemInsulationCap);
                Capability<IInsulatableCap> capability = ModCapabilities.ITEM_INSULATION;

                ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
                {
                    @Nonnull
                    @Override
                    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction)
                    {
                        // If the requested cap is the insulation cap, return the insulation cap
                        if (cap == capability)
                        {   return capOptional.cast();
                        }
                        return LazyOptional.empty();
                    }

                    @Override
                    public CompoundTag serializeNBT()
                    {   return itemInsulationCap.serializeNBT();
                    }

                    @Override
                    public void deserializeNBT(CompoundTag nbt)
                    {   itemInsulationCap.deserializeNBT(nbt);
                    }
                };

                // Attach the capability to the item
                event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "item_insulation"), provider);
            }
        }

        @SubscribeEvent
        public static void handleInventoryOpen(PlayerContainerEvent event)
        {
        event.getPlayer().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
        }

        @SubscribeEvent
        public static void clearCachePeriodically(TickEvent.WorldTickEvent event)
        {
            if (event.phase == TickEvent.Phase.END && event.world.getGameTime() % 200 == 0)
            {   CAP_CACHE.clear();
            }
        }

        static ContainerListener INSULATION_LISTENER = new ContainerListener()
        {
            @Override
            public void slotChanged(AbstractContainerMenu sendingContainer, int slot, ItemStack stack)
            {
                ItemStack containerStack = sendingContainer.getSlot(slot).getItem();
                getInsulationCap(containerStack).ifPresent(cap ->
                {
                    // Serialize insulation for syncing to client
                    containerStack.getOrCreateTag().remove("Insulation");
                    containerStack.getOrCreateTag().merge(cap.serializeNBT());
                });
            }

            @Override
            public void dataChanged(AbstractContainerMenu sendingContainer, int slot, int value)
            {

            }
        };

        @SubscribeEvent
        public static void onContainerOpen(PlayerContainerEvent.Open event)
        {
            event.getContainer().addSlotListener(INSULATION_LISTENER);
        }

        @SubscribeEvent
        public static void onContainerClose(PlayerContainerEvent.Close event)
        {
            event.getContainer().removeSlotListener(INSULATION_LISTENER);
        }
    }

    public static LazyOptional<IInsulatableCap> getInsulationCap(ItemStack stack)
    {   return isInsulatable(stack) ? CAP_CACHE.get(stack) : LazyOptional.empty();
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
            else return ConfigSettings.INSULATION_SLOTS.get().getSlots(LivingEntity.getEquipmentSlotForItem(stack), stack);
        }
        else return 0;
    }

    /**
     * Checks if the item is allowed to receive insulation from sewing.<br>
     * The item must be equipable and not have any built-in insulation.
     */
    public static boolean isInsulatable(ItemStack stack)
    {
        return stack.getItem() instanceof Wearable
            && !ConfigSettings.INSULATION_BLACKLIST.get().contains(stack.getItem())
            && getInsulatorInsulation(stack).isEmpty();
    }

    public static int getSlotsFilled(ItemStack stack, Collection<InsulatorData> insulators)
    {
        int slots = 0;
        for (InsulatorData data : insulators)
        {
            if (data.fillSlots(stack))
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
     * Returns a list of {@link InsulatorData} on the item, for the given slot.<br>
     * Use {@link com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder#filterValid(List, ItemStack)} to restrict the results to only valid/active insulators.
     * @param slots The slot(s) to get insulation for. If none specified, gets insulation for all slots.
     * @return an IMMUTABLE list of insulation the item has.
     */
    public static List<InsulatorData> getInsulatorsForStack(ItemStack stack, Insulation.Slot... slots)
    {
        if (stack.isEmpty()) return new ArrayList<>();

        List<InsulatorData> insulators = new ArrayList<>();
        // If no slots specified, get all insulators
        if (slots.length == 0)
        {
            for (int i = 0; i < Insulation.Slot.values().length; i++)
            {   Insulation.Slot slot = Insulation.Slot.values()[i];
                insulators.addAll(getInsulatorsForStack(stack, slot));
            }
            return ImmutableList.copyOf(insulators);
        }
        // Get insulators for specified slots
        for (int i = 0; i < slots.length; i++)
        {
            Insulation.Slot slot = slots[i];
            // Get applied armor insulation
            if (slot == Insulation.Slot.ARMOR && isInsulatable(stack))
            {
                getInsulationCap(stack).ifPresent(cap ->
                {   insulators.addAll(getAppliedArmorInsulators(stack));
                });
            }
            insulators.addAll(getInsulatorsForSlotType(slot).get(stack.getItem()));
        }

        return ImmutableList.copyOf(insulators);
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
               .map(IInsulatableCap::getInsulation).orElse(new ArrayList<>())
               .stream()
               .map(Pair::getSecond)
               .flatMap(Collection::stream).toList();
    }

    /**
     * Gathers a list of modifiers for the given attribute that are on this stack, from its insulation
     * @param operation Optional. Filters the output to only include modifiers with the given operation.
     * @param owner Optional. The entity wearing the item. This will be used to check the validity of the insulation before its modifiers are added to the list.
     */
    public static List<AttributeModifier> getArmorInsulationAttributes(ItemStack stack, Attribute attribute, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (InsulatorData insulator : getInsulatorsForStack(stack, Insulation.Slot.ARMOR))
        {
            if (insulator.test(owner, stack))
            {
                modifiers.addAll(insulator.attributes().get(attribute)
                                          .stream()
                                          .filter(mod -> operation == null || mod.getOperation() == operation)
                                          .toList());
            }
        }
        return modifiers;
    }

    /**
     * Gets all attribute modifiers for the given attribute that this item has, provided it is in the given slot. This includes modifiers from the item itself and from its insulation.
     */
    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Attribute attribute, EquipmentSlot slot, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>(operation != null
                                                  ? stack.getAttributeModifiers(slot).get(attribute)
                                                         .stream()
                                                         .filter(mod -> mod.getOperation() == operation)
                                                         .toList()
                                                  : stack.getAttributeModifiers(slot).get(attribute));
        modifiers.addAll(getArmorInsulationAttributes(stack, attribute, operation, owner));
        return modifiers;
    }

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Attribute attribute, EquipmentSlot slot)
    {   return getAttributeModifiersForSlot(stack, attribute, slot, null, null);
    }
}
