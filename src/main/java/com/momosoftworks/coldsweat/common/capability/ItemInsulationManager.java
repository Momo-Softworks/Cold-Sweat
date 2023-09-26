package com.momosoftworks.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.ItemSettingsConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
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
        if (isInsulatable(stack))
        {
            // Make a new capability instance to attach to the item
            final IInsulatableCap itemInsulationCap = new ItemInsulationCap();
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
            CompoundTag stackNBT = stack.getOrCreateTag();
            if (stackNBT.getBoolean("insulated"))
            {
                getInsulationCap(event.getObject()).ifPresent(iCap ->
                {
                    EquipmentSlot slot = stack.getItem() instanceof ArmorItem armor ? armor.getSlot() : null;
                    if (slot != null)
                    {
                        stackNBT.remove("insulated");
                        iCap.addInsulationItem(
                        switch (slot)
                        {   case HEAD -> Items.LEATHER_HELMET.getDefaultInstance();
                            case CHEST -> Items.LEATHER_CHESTPLATE.getDefaultInstance();
                            case LEGS -> Items.LEATHER_LEGGINGS.getDefaultInstance();
                            case FEET -> Items.LEATHER_BOOTS.getDefaultInstance();
                            default -> ItemStack.EMPTY;
                        });
                        if (iCap instanceof ItemInsulationCap cap)
                        {   cap.serializeSimple(stack);
                        }
                    }
                });
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
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && player.tickCount % 20 == 0
        && event.side == LogicalSide.SERVER && player.getPersistentData().getBoolean("InventoryOpen"))
        {
            player.getAllSlots().forEach(stack ->
            {
                if (isInsulatable(stack))
                {
                    // Cache the item cap
                    getInsulationCap(stack).ifPresent(iCap ->
                    {
                        if (iCap instanceof ItemInsulationCap cap)
                        {   cap.serializeSimple(stack);
                        }
                    });
                }
            });
        }
    }

    public static Pair<Double, Double> getItemInsulation(ItemStack item)
    {
        Pair<Double, Double> insulation = ConfigSettings.INSULATION_ITEMS.get().get(item.getItem());
        if (insulation != null)
        {   return insulation;
        }
        else
        {
            insulation = ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().get(item.getItem());
            if (insulation != null)
            {   return insulation;
            }
        }
        return new Pair<>(0.0, 0.0);
    }

    public static int getInsulationSlots(ItemStack item)
    {
        List<? extends Number> slots = ItemSettingsConfig.getInstance().getArmorInsulationSlots();
        return switch (LivingEntity.getEquipmentSlotForItem(item))
        {
            case HEAD  -> slots.get(0).intValue();
            case CHEST -> slots.get(1).intValue();
            case LEGS  -> slots.get(2).intValue();
            case FEET  -> slots.get(3).intValue();
            default -> 0;
        };
    }

    public static boolean isInsulatable(ItemStack stack)
    {
        Item item = stack.getItem();
        return item instanceof Wearable
            && !ConfigSettings.ADAPTIVE_INSULATION_ITEMS.get().containsKey(item)
            && !ConfigSettings.INSULATION_ITEMS.get().containsKey(item);
    }
}
