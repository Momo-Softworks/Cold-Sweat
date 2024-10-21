package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityPickEvent;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class ItemFrameEvents
{
    @SubscribeEvent
    public static void onPickItem(EntityPickEvent event)
    {
        if (event.getEntity() instanceof ItemFrame frame && event.getStack().getItem() == ModItems.THERMOMETER.value())
        {
            ItemStack stack = frame.getItem().copy();
            if (frame.getPersistentData().contains("ItemName"))
            {
                stack.set(DataComponents.CUSTOM_NAME, Component.Serializer.fromJson(frame.getPersistentData().getString("ItemName"), RegistryHelper.getRegistryAccess()));
            }
            else stack.remove(DataComponents.CUSTOM_NAME);
            event.setStack(stack);
        }
    }

    @SubscribeEvent
    public static void onThermometerPlaced(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof ItemFrame frame && frame.getItem().isEmpty()
        && event.getItemStack().getItem() == ModItems.THERMOMETER.value())
        {
            if (event.getItemStack().has(DataComponents.CUSTOM_NAME))
                frame.getPersistentData().putString("ItemName", Component.Serializer.toJson(event.getItemStack().getHoverName(), RegistryHelper.getRegistryAccess()));
            else
                frame.getPersistentData().remove("ItemName");
        }
    }

    @SubscribeEvent
    public static void onItemFrameLoaded(PlayerEvent.StartTracking event)
    {
        if (event.getTarget() instanceof ItemFrame frame && event.getEntity() instanceof ServerPlayer player
        && frame.getItem().getItem() == ModItems.THERMOMETER.value() && !frame.level().isClientSide)
        {
            if (frame.getPersistentData().contains("ItemName"))
            {   // Sync the item name to the client
                WorldHelper.syncEntityForgeData(frame, player);
            }
        }
    }
}
