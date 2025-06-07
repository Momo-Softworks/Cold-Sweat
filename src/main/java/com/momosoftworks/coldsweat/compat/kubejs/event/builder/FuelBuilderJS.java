package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public class FuelBuilderJS
{
    public double fuel = 0;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();

    public FuelBuilderJS()
    {}

    public FuelBuilderJS items(String... items)
    {
        List<Item> itemList = RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, ConfigHelper.getItems(items));
        itemPredicate.add(new ItemRequirement(itemList, null), false);
        return this;
    }

    public FuelBuilderJS fuel(double temperature)
    {
        this.fuel = temperature;
        return this;
    }

    public FuelBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public FuelData build(FuelData.FuelType fuelType)
    {
        FuelData data = new FuelData(this.itemPredicate, fuelType, this.fuel);
        data.setRegistryType(ConfigData.Type.KUBEJS);
        return data;
    }
}
