package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.ItemSettingsConfig;
import com.momosoftworks.coldsweat.config.util.ItemData;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class ItemInsulationManager
{
    public static Map<ItemStack, LazyOptional<IInsulatableCap>> ITEM_INSULATION_CAPS = new HashMap<>();

    @SubscribeEvent
    public static void attachCapabilityToItemHandler(AttachCapabilitiesEvent<ItemStack> event)
    {
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        if (item instanceof IArmorVanishable
        && !ConfigSettings.INSULATION_ITEMS.get().containsKey(ItemData.of(stack)))
        {
            // Make a new capability instance to attach to the item
            ItemInsulationCap itemInsulationCap = new ItemInsulationCap();
            // Optional that holds the capability instance
            LazyOptional<IInsulatableCap> capOptional = LazyOptional.of(() -> itemInsulationCap);
            Capability<IInsulatableCap> capability = ModCapabilities.ITEM_INSULATION;

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundNBT>()
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
                public CompoundNBT serializeNBT()
                {   return itemInsulationCap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundNBT nbt)
                {   itemInsulationCap.deserializeNBT(nbt);
                }
            };

            // Attach the capability to the item
            event.addCapability(new ResourceLocation(ColdSweat.MOD_ID, "item_insulation"), provider);

            // Legacy code for updating items using the pre-2.2 insulation system
            CompoundNBT stackNBT = stack.getOrCreateTag();
            if (stack.getItem() instanceof ArmorItem)
            {
                ArmorItem armor = (ArmorItem) stack.getItem();
                if (stackNBT.getBoolean("insulated"))
                {   stackNBT.remove("insulated");
                    switch (armor.getSlot())
                    {   case HEAD  : itemInsulationCap.addInsulationItem(Items.LEATHER_HELMET.getDefaultInstance()); break;
                        case CHEST : itemInsulationCap.addInsulationItem(Items.LEATHER_CHESTPLATE.getDefaultInstance()); break;
                        case LEGS  : itemInsulationCap.addInsulationItem(Items.LEATHER_LEGGINGS.getDefaultInstance()); break;
                        case FEET  : itemInsulationCap.addInsulationItem(Items.LEATHER_BOOTS.getDefaultInstance()); break;
                        default    : itemInsulationCap.addInsulationItem(ItemStack.EMPTY); break;
                    }
                    itemInsulationCap.serializeSimple(stack);
                }
            }
        }
    }

    public static LazyOptional<IInsulatableCap> getInsulationCap(ItemStack stack)
    {
        return ITEM_INSULATION_CAPS.computeIfAbsent(stack, s ->
        {   LazyOptional<IInsulatableCap> cap = stack.getCapability(ModCapabilities.ITEM_INSULATION);
            cap.addListener(c -> ITEM_INSULATION_CAPS.remove(stack));
            return cap;
        });
    }

    @SubscribeEvent
    public static void handleInventoryOpen(PlayerContainerEvent event)
    {
        event.getPlayer().getPersistentData().putBoolean("InventoryOpen", event instanceof PlayerContainerEvent.Open);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        // if the inventory screen is open
        PlayerEntity player = event.player;
        if (event.phase == TickEvent.Phase.END && player.tickCount % 20 == 0
        && event.side == LogicalSide.SERVER && player.getPersistentData().getBoolean("InventoryOpen"))
        {
            player.getAllSlots().forEach(stack ->
            {
                if (isInsulatable(stack))
                {   getInsulationCap(stack).ifPresent(iCap ->
                    {   if (iCap instanceof ItemInsulationCap)
                        {   ((ItemInsulationCap) iCap).serializeSimple(stack);
                        }
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();
        player.getAllSlots().forEach(stack ->
        {
            if (isInsulatable(stack))
            {   getInsulationCap(stack).ifPresent(iCap ->
                {
                    // Legacy code for updating items using the pre-2.2 insulation system
                    CompoundNBT stackNBT = stack.getOrCreateTag();
                    if (stack.getItem() instanceof ArmorItem)
                    {
                        ArmorItem armor = (ArmorItem) stack.getItem();
                        if (stackNBT.getBoolean("insulated"))
                        {   stackNBT.remove("insulated");
                            switch (armor.getSlot())
                            {   case HEAD  : iCap.addInsulationItem(Items.LEATHER_HELMET.getDefaultInstance()); break;
                                case CHEST : iCap.addInsulationItem(Items.LEATHER_CHESTPLATE.getDefaultInstance()); break;
                                case LEGS  : iCap.addInsulationItem(Items.LEATHER_LEGGINGS.getDefaultInstance()); break;
                                case FEET  : iCap.addInsulationItem(Items.LEATHER_BOOTS.getDefaultInstance()); break;
                                default    : iCap.addInsulationItem(ItemStack.EMPTY); break;
                            }
                            if (iCap instanceof ItemInsulationCap)
                            {   ((ItemInsulationCap) iCap).serializeSimple(stack);
                            }
                        }
                    }
                });
            }
        });
    }

    public static int getInsulationSlots(ItemStack item)
    {
        List<? extends Number> slots = ItemSettingsConfig.getInstance().getArmorInsulationSlots();
        EquipmentSlotType slot = MobEntity.getEquipmentSlotForItem(item);

        switch (slot)
        {   case HEAD  : return slots.get(0).intValue();
            case CHEST : return slots.get(1).intValue();
            case LEGS  : return slots.get(2).intValue();
            case FEET  : return slots.get(3).intValue();
            default : return 0;
        }
    }

    public static boolean isInsulatable(ItemStack stack)
    {
        return stack.getItem() instanceof IArmorVanishable
            && !ConfigSettings.INSULATION_ITEMS.get().containsKey(ItemData.of(stack));
    }
}
