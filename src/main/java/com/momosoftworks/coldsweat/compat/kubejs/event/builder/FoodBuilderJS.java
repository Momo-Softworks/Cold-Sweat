package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Predicate;

public class FoodBuilderJS
{
    public double temperature = 0;
    public int duration = 0;
    public int stackLimit = 1;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();

    public FoodBuilderJS()
    {}

    public FoodBuilderJS items(String... items)
    {
        List<Item> itemList = RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ITEMS, ConfigHelper.getItems(items));
        if (itemList.isEmpty() && items.length != 0)
        {   this.itemPredicate.add(ItemRequirement.NONE, true);
        }
        else
        {   this.itemPredicate.add(new ItemRequirement(itemList, null), false);
        }
        return this;
    }

    public FoodBuilderJS temperature(double temperature)
    {
        this.temperature = temperature;
        return this;
    }

    public FoodBuilderJS duration(int duration)
    {
        this.duration = duration;
        return this;
    }

    public FoodBuilderJS stackLimit(int stackLimit)
    {
        this.stackLimit = stackLimit;
        return this;
    }

    public FoodBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public FoodBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public FoodData build()
    {
        FoodData data = new FoodData(this.itemPredicate, this.temperature, this.duration, this.stackLimit, this.entityPredicate);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
