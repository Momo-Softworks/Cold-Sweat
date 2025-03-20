package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.client.InsulatorTabBuildEvent;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.List;

@Mod.EventBusSubscriber
public class FilterInsulationItemsTab
{
    /**
     * If there are more than 6 items that belong to the same tag, only the first item will be added to the insulators tab
     */
    @SubscribeEvent
    public static void filterItems(InsulatorTabBuildEvent event)
    {
        event.addCheck((item, insulator) ->
        {
            List<Either<TagKey<Item>, Item>> items = insulator.item().flatMap(it -> CSMath.mutable(it.items()), CSMath::merge, List::removeAll).orElse(List.of());
            for (Either<TagKey<Item>, Item> either : items)
            {
                if (either.left().map(tag -> item.builtInRegistryHolder().is(tag)).orElse(false))
                {
                    TagKey<Item> tagKey = either.left().get();
                    ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(tagKey);
                    int tagSize = tag.size();

                    if (tagSize > 6 && tag.stream().findFirst().get() != item)
                    {   return false;
                    }
                }
            }
            return true;
        });
    }
}
