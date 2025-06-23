package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.client.InsulatorTabBuildEvent;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.*;

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
            Map<ITag<Item>, Set<Item>> tagItems = new HashMap<>();
            for (ItemRequirement requirement : insulator.item().requirements())
            {
                for (Either<ITag<Item>, Item> either : requirement.items().requirements())
                {
                    if (either.left().isPresent())
                    {   ITag<Item> tagKey = either.left().get();
                        mapAdd(tagItems, tagKey);
                    }
                }
                for (Either<ITag<Item>, Item> either : requirement.items().exclusions())
                {
                    if (either.left().isPresent())
                    {   ITag<Item> tagKey = either.left().get();
                        mapRemove(tagItems, tagKey);
                    }
                }
            }
            for (ItemRequirement requirement : insulator.item().exclusions())
            {
                for (Either<ITag<Item>, Item> either : requirement.items().requirements())
                {
                    if (either.left().isPresent())
                    {   ITag<Item> tagKey = either.left().get();
                        mapRemove(tagItems, tagKey);
                    }
                }
                for (Either<ITag<Item>, Item> either : requirement.items().exclusions())
                {
                    if (either.left().isPresent())
                    {   ITag<Item> tagKey = either.left().get();
                        mapAdd(tagItems, tagKey);
                    }
                }
            }
            for (Map.Entry<ITag<Item>, Set<Item>> entry : tagItems.entrySet())
            {
                ITag<Item> tagKey = entry.getKey();
                if (tagKey.contains(item))
                {
                    int tagSize = entry.getValue().size();

                    if (tagSize > 6 && tagKey.getValues().stream().findFirst().get() != item)
                    {   return false;
                    }
                }
            }
            return true;
        });
    }

    private static void mapAdd(Map<ITag<Item>, Set<Item>> map, ITag<Item> tag)
    {
        // add all entries from the tag to the map, or create a new entry if it doesn't exist
        Set<Item> items = map.computeIfAbsent(tag, k -> new HashSet<>(RegistryHelper.mapTaggableList(new NegatableList<>(Either.left(tag)))));
        Set<Item> currentItems = map.computeIfAbsent(tag, k -> new HashSet<>());
        currentItems.addAll(items);
        map.put(tag, currentItems);
    }

    private static void mapRemove(Map<ITag<Item>, Set<Item>> map, ITag<Item> tag)
    {
        Set<Item> items = map.computeIfAbsent(tag, k -> new HashSet<>(RegistryHelper.mapTaggableList(new NegatableList<>(Either.left(tag)))));
        for (Set<Item> itemSet : map.values())
        {   itemSet.removeAll(items);
        }
    }
}
