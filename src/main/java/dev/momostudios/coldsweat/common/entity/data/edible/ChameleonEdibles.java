package dev.momostudios.coldsweat.common.entity.data.edible;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class ChameleonEdibles
{
    public static final Map<Item, Edible> EDIBLES = new HashMap<>();

    public static Edible getEdible(Item item)
    {
        return EDIBLES.get(item);
    }

    public static void addEdible(Edible edible, Item... items)
    {
        if (items == null) throw new IllegalArgumentException("Chameleon edible items cannot be null!");
        for (Item item : items)
        {
            EDIBLES.put(item, edible);
        }
    }
}
