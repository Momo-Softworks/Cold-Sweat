package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AddWanderingTrades
{
    @SubscribeEvent
    public static void addWanderingTrades(WandererTradesEvent event)
    {   event.getRareTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.FUR, 1), 99999999, 15, 1));
        event.getRareTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.HOGLIN_HIDE, 1), 99999999, 15, 1));
    }
}
