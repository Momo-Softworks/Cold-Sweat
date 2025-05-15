package com.momosoftworks.coldsweat.client.event;

import com.mojang.datafixers.util.Either;
import com.momosoftworks.coldsweat.api.event.client.InsulatorTabBuildEvent;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
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
            List<Either<ITag<Item>, Item>> items = insulator.item().flatMap(it -> CSMath.mutable(it.items().flatten()), CSMath::append, List::removeAll).orElse(Arrays.asList());
            for (Either<ITag<Item>, Item> either : items)
            {
                if (either.left().map(tag -> tag.contains(item)).orElse(false))
                {
                    ITag<Item> tag = either.left().get();
                    int tagSize = tag.getValues().size();

                    if (tagSize > 6 && tag.getValues().stream().findFirst().get() != item)
                    {   return false;
                    }
                }
            }
            return true;
        });
    }
}
