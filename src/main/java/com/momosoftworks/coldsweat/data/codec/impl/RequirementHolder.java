package com.momosoftworks.coldsweat.data.codec.impl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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
    {   return list.stream().filter(obj -> obj.test(stack)).toList();
    }

    static <T extends RequirementHolder> List<T> filterValid(List<T> list, ItemStack stack, Entity entity)
    {   return list.stream().filter(obj -> obj.test(stack, entity)).toList();
    }

    static <T extends RequirementHolder> List<T> filterValid(List<T> list, Entity entity)
    {   return list.stream().filter(obj -> obj.test(entity)).toList();
    }
}
