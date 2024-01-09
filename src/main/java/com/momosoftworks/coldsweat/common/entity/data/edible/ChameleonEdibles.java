package com.momosoftworks.coldsweat.common.entity.data.edible;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ChameleonEdibles
{
    public static final List<Edible> EDIBLES = new ArrayList<>();

    public static Optional<Edible> getEdible(ItemStack item)
    {   return EDIBLES.stream().filter(edible -> edible.associatedItems().contains(item.getItem())).findFirst();
    }

    public static void addEdible(Edible edible)
    {   EDIBLES.add(edible);
    }
}
