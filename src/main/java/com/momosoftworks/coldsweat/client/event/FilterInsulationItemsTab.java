package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.client.InsulatorTabBuildEvent;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

@EventBusSubscriber
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
            Map<TagKey<Item>, Set<Item>> tagItems = new HashMap<>();
            for (ItemRequirement requirement : insulator.item().requirements())
            {
                for (Either<TagKey<Item>, Item> either : requirement.items().requirements())
                {
                    if (either.left().isPresent())
                    {   TagKey<Item> tagKey = either.left().get();
                        mapAdd(tagItems, tagKey);
                    }
                }
                for (Either<TagKey<Item>, Item> either : requirement.items().exclusions())
                {
                    if (either.left().isPresent())
                    {   TagKey<Item> tagKey = either.left().get();
                        mapRemove(tagItems, tagKey);
                    }
                }
            }
            for (ItemRequirement requirement : insulator.item().exclusions())
            {
                for (Either<TagKey<Item>, Item> either : requirement.items().requirements())
                {
                    if (either.left().isPresent())
                    {   TagKey<Item> tagKey = either.left().get();
                        mapRemove(tagItems, tagKey);
                    }
                }
                for (Either<TagKey<Item>, Item> either : requirement.items().exclusions())
                {
                    if (either.left().isPresent())
                    {   TagKey<Item> tagKey = either.left().get();
                        mapAdd(tagItems, tagKey);
                    }
                }
            }
            for (Map.Entry<TagKey<Item>, Set<Item>> entry : tagItems.entrySet())
            {
                TagKey<Item> tagKey = entry.getKey();
                if (item.builtInRegistryHolder().is(tagKey))
                {
                    HolderSet<Item> tag = BuiltInRegistries.ITEM.getTag(tagKey).orElse(null);
                    int tagSize = entry.getValue().size();

                    if (tagSize > 6 && tag.stream().findFirst().get().value() != item)
                    {   return false;
                    }
                }
            }
            return true;
        });
    }

    private static void mapAdd(Map<TagKey<Item>, Set<Item>> map, TagKey<Item> tag)
    {
        // add all entries from the tag to the map, or create a new entry if it doesn't exist
        Set<Item> items = map.computeIfAbsent(tag, k -> new HashSet<>(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, new NegatableList<>(Either.left(tag)))));
        Set<Item> currentItems = map.computeIfAbsent(tag, k -> new HashSet<>());
        currentItems.addAll(items);
        map.put(tag, currentItems);
    }

    private static void mapRemove(Map<TagKey<Item>, Set<Item>> map, TagKey<Item> tag)
    {
        Set<Item> items = map.computeIfAbsent(tag, k -> new HashSet<>(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, new NegatableList<>(Either.left(tag)))));
        for (Set<Item> itemSet : map.values())
        {   itemSet.removeAll(items);
        }
    }
}
