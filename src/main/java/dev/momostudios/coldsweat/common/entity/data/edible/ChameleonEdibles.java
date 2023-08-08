package dev.momostudios.coldsweat.common.entity.data.edible;


import net.minecraft.item.Item;

import java.util.*;

public class ChameleonEdibles
{
    public static final List<Edible> EDIBLES = new ArrayList<>();

    public static Optional<Edible> getEdible(Item item)
    {   return EDIBLES.stream().filter(edible -> edible.associatedItems().contains(item)).findFirst();
    }

    public static void addEdible(Edible edible)
    {   EDIBLES.add(edible);
    }
}
