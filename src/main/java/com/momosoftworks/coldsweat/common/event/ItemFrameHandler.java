package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.event.vanilla.EntityPickEvent;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ItemFrameHandler
{
    @SubscribeEvent
    public static void onPickItem(EntityPickEvent event)
    {
        if (event.getEntity() instanceof ItemFrame frame && event.getStack().getItem() == ModItems.THERMOMETER)
        {
            ItemStack stack = frame.getItem().copy();
            if (frame.getPersistentData().contains("ItemName"))
            {
                stack.setHoverName(Component.Serializer.fromJson(frame.getPersistentData().getString("ItemName")));
            }
            else stack.setHoverName(null);
            event.setStack(stack);
        }
    }

    @SubscribeEvent
    public static void onThermometerPlaced(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof ItemFrame frame && frame.getItem().isEmpty()
        && event.getItemStack().getItem() == ModItems.THERMOMETER)
        {
            if (event.getItemStack().hasCustomHoverName())
                frame.getPersistentData().putString("ItemName", Component.Serializer.toJson(event.getItemStack().getHoverName()));
            else
                frame.getPersistentData().remove("ItemName");
        }
    }

    @SubscribeEvent
    public static void onItemFrameLoaded(PlayerEvent.StartTracking event)
    {
        if (event.getTarget() instanceof ItemFrame frame && event.getEntity() instanceof ServerPlayer player
        && frame.getItem().getItem() == ModItems.THERMOMETER && !frame.level.isClientSide)
        {
            if (frame.getPersistentData().contains("ItemName"))
            {   // Sync the item name to the client
                WorldHelper.syncEntityForgeData(frame, player);
            }
        }
    }
}
