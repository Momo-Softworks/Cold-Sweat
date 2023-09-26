package com.momosoftworks.coldsweat.common.entity.data.edible;

import net.minecraft.world.item.Item;

import java.util.*;

public class ChameleonEdibles
{
    public static final List<Edible> EDIBLES = new ArrayList<>();

    public static Optional<Edible> getEdible(Item item)
    {   return EDIBLES.stream().filter(edible -> item.builtInRegistryHolder().is(edible.associatedItems())).findFirst();
    }

    public static void addEdible(Edible edible)
    {   EDIBLES.add(edible);
    }
}
