package dev.momostudios.coldsweat.common.event;

import dev.momostudios.coldsweat.api.event.common.EntityPickEvent;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
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
        if (event.getEntity() instanceof ItemFrameEntity && event.getStack().getItem() == ModItems.THERMOMETER)
        {
            ItemFrameEntity frame = (ItemFrameEntity) event.getEntity();
            ItemStack stack = frame.getItem().copy();
            if (frame.getPersistentData().contains("ItemName"))
            {
                stack.setHoverName(ITextComponent.Serializer.fromJson(frame.getPersistentData().getString("ItemName")));
            }
            else stack.setHoverName(null);
            event.setStack(stack);
        }
    }

    @SubscribeEvent
    public static void onThermometerPlaced(PlayerInteractEvent.EntityInteract event)
    {
        if (event.getTarget() instanceof ItemFrameEntity && ((ItemFrameEntity) event.getTarget()).getItem().isEmpty()
        && event.getItemStack().getItem() == ModItems.THERMOMETER)
        {
            ItemFrameEntity frame = (ItemFrameEntity) event.getTarget();
            if (event.getItemStack().hasCustomHoverName())
                frame.getPersistentData().putString("ItemName", ITextComponent.Serializer.toJson(event.getItemStack().getHoverName()));
            else
                frame.getPersistentData().remove("ItemName");
        }
    }

    @SubscribeEvent
    public static void onItemFrameLoaded(PlayerEvent.StartTracking event)
    {
        if (event.getTarget() instanceof ItemFrameEntity && event.getEntity() instanceof ServerPlayerEntity
        && ((ItemFrameEntity) event.getTarget()).getItem().getItem() == ModItems.THERMOMETER && !event.getTarget().level.isClientSide)
        {
            ItemFrameEntity frame = (ItemFrameEntity) event.getTarget();
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            if (frame.getPersistentData().contains("ItemName"))
            {   // Sync the item name to the client
                WorldHelper.syncEntityForgeData(frame, player);
            }
        }
    }
}
