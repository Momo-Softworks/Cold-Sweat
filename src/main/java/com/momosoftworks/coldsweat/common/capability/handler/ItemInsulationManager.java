package com.momosoftworks.coldsweat.common.capability.handler;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.ModCapabilities;
import com.momosoftworks.coldsweat.common.capability.SidedCapabilityCache;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Wearable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Mod.EventBusSubscriber
public class ItemInsulationManager
{
    public static SidedCapabilityCache<IInsulatableCap, ItemStack> CAP_CACHE = new SidedCapabilityCache<>(ModCapabilities.ITEM_INSULATION, stack -> stack.isEmpty() || !isInsulatable(stack));

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

            // Legacy code for updating items using the pre-2.2 insulation system
            CompoundTag stackNBT = NBTHelper.getTagOrEmpty(stack);
            if (stack.getItem() instanceof ArmorItem armor)
            {
                if (stackNBT.getBoolean("insulated"))
                {   stackNBT.remove("insulated");
                    switch (armor.getSlot())
                    {   case HEAD  : itemInsulationCap.addInsulationItem(Items.LEATHER_HELMET.getDefaultInstance()); break;
                        case CHEST : itemInsulationCap.addInsulationItem(Items.LEATHER_CHESTPLATE.getDefaultInstance()); break;
                        case LEGS  : itemInsulationCap.addInsulationItem(Items.LEATHER_LEGGINGS.getDefaultInstance()); break;
                        case FEET  : itemInsulationCap.addInsulationItem(Items.LEATHER_BOOTS.getDefaultInstance()); break;
                        default    : itemInsulationCap.addInsulationItem(ItemStack.EMPTY); break;
                    }
                }
            }
        }
    }

    public static LazyOptional<IInsulatableCap> getInsulationCap(ItemStack stack)
    {   return isInsulatable(stack) ? CAP_CACHE.get(stack) : LazyOptional.empty();
    }

    @SubscribeEvent
    public static void handleInventoryOpen(PlayerContainerEvent event)
    {   event.getEntity().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
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

    /**
     * @return The number of insulation slots on this armor item, or 0 if it does not support insulation
     */
    public static int getInsulationSlots(ItemStack item)
    {
        return isInsulatable(item)
               ? ConfigSettings.INSULATION_SLOTS.get().getSlots(LivingEntity.getEquipmentSlotForItem(item), item)
               : 0;
    }

    /**
     * Checks if the item is allowed to receive insulation from sewing.<br>
     * The item must be equipable and not have any built-in insulation.
     */
    public static boolean isInsulatable(ItemStack stack)
    {
        return stack.getItem() instanceof Wearable
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
                for (Pair<ItemStack, Collection<InsulatorData>> pair : cap.getInsulation())
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
               .map(IInsulatableCap::getInsulation).orElse(new ArrayList<>())
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
    public static List<AttributeModifier> getAppliedInsulationAttributes(ItemStack stack, Attribute attribute, @Nullable AttributeModifier.Operation operation, @Nullable Entity owner)
    {
        List<AttributeModifier> modifiers = new ArrayList<>();
        for (InsulatorData insulator : getAllInsulatorsForStack(stack))
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
        modifiers.addAll(getAppliedInsulationAttributes(stack, attribute, operation, owner));
        return modifiers;
    }

    public static List<AttributeModifier> getAttributeModifiersForSlot(ItemStack stack, Attribute attribute, EquipmentSlot slot)
    {   return getAttributeModifiersForSlot(stack, attribute, slot, null, null);
    }
}
