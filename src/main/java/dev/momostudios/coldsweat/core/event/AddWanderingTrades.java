package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.util.registries.ModItems;
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
    {   event.getRareTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.GOAT_FUR, 1), 99999999, 15, 1));
    }
}
