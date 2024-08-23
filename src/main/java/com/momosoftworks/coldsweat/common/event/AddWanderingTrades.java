package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber
public class AddWanderingTrades
{
    @SubscribeEvent
    public static void addWanderingTrades(WandererTradesEvent event)
    {   event.getRareTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.GOAT_FUR.value(), 1), 99999999, 15, 1));
        event.getRareTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.HOGLIN_HIDE.value(), 1), 99999999, 15, 1));
    }
}
