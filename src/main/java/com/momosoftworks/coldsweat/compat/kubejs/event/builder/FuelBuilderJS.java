package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FuelBuilderJS
{
    public ValueGetter<Integer> fuel = ValueGetter.constant(0);
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();

    public FuelBuilderJS()
    {}

    public FuelBuilderJS items(String... items)
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

    public FuelBuilderJS fuel(Function<Map<String, Object>, Integer> function)
    {
        this.fuel = ValueGetter.of(function);
        return this;
    }
    public FuelBuilderJS fuel(int temperature)
    {   return fuel(m -> temperature);
    }

    public FuelBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public FuelData build(FuelData.FuelType fuelType)
    {
        FuelData data = new FuelData(this.itemPredicate, fuelType, this.fuel);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
