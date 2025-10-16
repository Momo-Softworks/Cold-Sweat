package com.momosoftworks.coldsweat.data.codec.impl;


import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public interface RequirementHolder
{
    default boolean test(ItemStack stack)
    {   return true;
    }

    default boolean test(Entity entity)
    {   return true;
    }

    default boolean test(ItemStack stack, Entity entity)
    {   return test(stack) && test(entity);
    }

    default boolean test(Entity entity, ItemStack stack)
    {   return test(stack, entity);
    }

    static <T extends RequirementHolder> List<T> filterValid(List<T> list, ItemStack stack)
    {   return list.stream().filter(obj -> obj.test(stack)).collect(Collectors.toList());
    }

    static <T extends RequirementHolder> List<T> filterValid(List<T> list, ItemStack stack, Entity entity)
    {   return list.stream().filter(obj -> obj.test(stack, entity)).collect(Collectors.toList());
    }

    static <T extends RequirementHolder> List<T> filterValid(List<T> list, Entity entity)
    {   return list.stream().filter(obj -> obj.test(entity)).collect(Collectors.toList());
    }
}
