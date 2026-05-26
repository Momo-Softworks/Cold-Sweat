package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.configuration.FoodData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class FoodBuilderJS
{
    public ValueGetter<Double> temperature = new ValueGetter<>(ValueGetter.Type.CONSTANT, null, sources -> 0.0);
    public ValueGetter<Integer> duration = ValueGetter.constant(0);
    public ValueGetter<Integer> stackLimit = ValueGetter.constant(1);
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public Map<Temperature.Trait, List<TempModifier.Factory>> modifiers = new HashMap<>();

    public FoodBuilderJS()
    {}

    public FoodBuilderJS items(String... items)
    {
        List<Item> itemList = RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, ConfigHelper.getItems(items));
        if (itemList.isEmpty() && items.length != 0)
        {   this.itemPredicate.add(ItemRequirement.NONE, true);
        }
        else
        {   this.itemPredicate.add(new ItemRequirement(itemList, null), false);
        }
        return this;
    }

    public FoodBuilderJS temperature(Function<Map<String, Object>, Double> getter)
    {
        this.temperature = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public FoodBuilderJS temperature(double temperature)
    {   return temperature(m -> temperature);
    }

    public FoodBuilderJS duration(Function<Map<String, Object>, Integer> getter)
    {   this.duration = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public FoodBuilderJS duration(int duration)
    {   return duration(m -> duration);
    }

    public FoodBuilderJS stackLimit(Function<Map<String, Object>, Integer> getter)
    {   this.stackLimit = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public FoodBuilderJS stackLimit(int stackLimit)
    {   return stackLimit(m -> stackLimit);
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

    public ModBuilder modifier(String trait, String modifier)
    {   return new ModBuilder(Temperature.Trait.fromID(trait), modifier);
    }

    public FoodData build()
    {
        FoodData data = new FoodData(this.itemPredicate, this.temperature, this.duration, this.stackLimit, this.entityPredicate, this.modifiers);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }

    public class ModBuilder
    {
        Temperature.Trait trait;
        String id;
        CompoundTag nbt = new CompoundTag();
        int expireTime = -1;
        int tickRate = 1;

        public ModBuilder(Temperature.Trait trait, String id)
        {   this.trait = trait;
            this.id = id;
        }

        public ModBuilder nbt(CompoundTag nbt)
        {   this.nbt = nbt;
            return this;
        }
        public ModBuilder expireTime(int expireTime)
        {   this.expireTime = expireTime;
            return this;
        }
        public ModBuilder tickRate(int tickRate)
        {   this.tickRate = tickRate;
            return this;
        }

        public FoodBuilderJS build()
        {
            TempModifier.Factory modifier = new TempModifier.Factory(ResourceLocation.parse(id), nbt, expireTime, tickRate);
            FoodBuilderJS.this.modifiers.computeIfAbsent(trait, t -> new ArrayList<>()).add(modifier);
            return FoodBuilderJS.this;
        }
    }
}
