package dev.momostudios.coldsweat.api.event.core;

import dev.momostudios.coldsweat.common.entity.data.edible.ChameleonEdibles;
import dev.momostudios.coldsweat.common.entity.data.edible.Edible;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.Optional;

public class EdiblesRegisterEvent extends Event
{
    public void registerEdible(Edible edible, Item... items)
    {
        ChameleonEdibles.addEdible(edible, items);
    }

    @SafeVarargs
    public final void registerEdible(Edible edible, TagKey<Item>... tags)
    {
        for (TagKey<Item> tag : tags)
        {
            Optional<ITag<Item>> optionalTag = ForgeRegistries.ITEMS.tags().stream().filter(registryTag ->
                    registryTag.getKey().equals(tag)).findFirst();
            optionalTag.ifPresent(itemITag -> itemITag.stream().forEach(item -> ChameleonEdibles.addEdible(edible, item)));
        }
    }
}
