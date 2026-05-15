package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.kubejs.util.KubeHelper;
import com.momosoftworks.coldsweat.data.codec.configuration.ItemTempData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class ItemTempBuilderJS
{
    public final Set<Either<IntegerBounds, ItemTempData.SlotType>> slots = new HashSet<>();
    public ValueGetter<Double> temperature = new ValueGetter<>(ValueGetter.Type.CONSTANT, null, sources -> 0.0);
    public ValueGetter<Double> maxEffect = ValueGetter.constant(Double.POSITIVE_INFINITY);
    public ValueGetter<Double> maxTemp;
    public ValueGetter<Double> minTemp;
    public Temperature.Trait trait = Temperature.Trait.WORLD;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public AttributeModifierMap attributes = new AttributeModifierMap();
    public Map<ResourceLocation, ValueGetter<Double>> immuneTempModifiers = new HashMap<>();
    public ValueGetter<Boolean> hideIfUnmet = ValueGetter.constant(false);

    public ItemTempBuilderJS()
    {}

    public ItemTempBuilderJS items(String... items)
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

    public ItemTempBuilderJS temperature(Function<Map<String, Object>, Double> getter)
    {
        this.temperature = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public ItemTempBuilderJS temperature(double temperature)
    {   return temperature(m -> temperature);
    }

    public ItemTempBuilderJS maxEffect(Function<Map<String, Object>, Double> getter)
    {   this.maxEffect = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public ItemTempBuilderJS maxEffect(double maxEffect)
    {   return maxEffect(m -> maxEffect);
    }

    public ItemTempBuilderJS maxTemp(Function<Map<String, Object>, Double> getter)
    {   this.maxTemp = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public ItemTempBuilderJS maxTemp(double maxTemp)
    {   return maxTemp(m -> maxTemp);
    }

    public ItemTempBuilderJS minTemp(Function<Map<String, Object>, Double> getter)
    {   this.minTemp = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", getter);
        return this;
    }
    public ItemTempBuilderJS minTemp(double minTemp)
    {   return minTemp(m -> minTemp);
    }

    public ItemTempBuilderJS trait(String trait)
    {
        this.trait = Temperature.Trait.fromID(trait);
        return this;
    }

    public ItemTempBuilderJS slots(int... slots)
    {
        for (int slot : slots)
        {   this.slots.add(Either.left(new IntegerBounds(slot, slot)));
        }
        return this;
    }

    public ItemTempBuilderJS slotsInRange(int min, int max)
    {
        this.slots.add(Either.left(new IntegerBounds(min, max)));
        return this;
    }

    public ItemTempBuilderJS equipmentSlots(String... slots)
    {
        for (String slot : slots)
        {   this.slots.add(Either.right(ItemTempData.SlotType.byName(slot)));
        }
        return this;
    }

    public ItemTempBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public ItemTempBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public ItemTempBuilderJS attribute(String attributeId, double amount, String operation)
    {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeId));
        if (!KubeHelper.expect(attributeId, attribute, Attribute.class))
        {   return this;
        }
        attributes.put(attribute, new AttributeModifier("kubejs", amount, AttributeModifier.Operation.valueOf(operation.toUpperCase(Locale.ROOT))));
        return this;
    }

    public ItemTempBuilderJS immuneToModifier(String modifierId, Function<Map<String, Object>, Double> immunity)
    {
        ResourceLocation location = new ResourceLocation(modifierId);
        if (!TempModifierRegistry.getEntries().containsKey(location))
        {   ColdSweat.LOGGER.warn("Tried to add immunity to non-existent temperature modifier: {}", location);
            return this;
        }
        immuneTempModifiers.put(new ResourceLocation(modifierId), new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", immunity));
        return this;
    }
    public ItemTempBuilderJS immuneToModifier(String modifierId, double immunity)
    {   return immuneToModifier(modifierId, m -> immunity);
    }

    public ItemTempBuilderJS hideIfUnmet(Function<Map<String, Object>, Boolean> hideIfUnmet)
    {
        this.hideIfUnmet = new ValueGetter<>(ValueGetter.Type.EXPRESSION, "custom", hideIfUnmet);
        return this;
    }
    public ItemTempBuilderJS hideIfUnmet(boolean hideIfUnmet)
    {   return hideIfUnmet(m -> hideIfUnmet);
    }

    public ItemTempData build()
    {
        ItemTempData data = new ItemTempData(this.itemPredicate, ImmutableList.copyOf(this.slots),
                                             this.temperature, this.trait, this.maxEffect,
                                             this.maxTemp, this.minTemp, this.entityPredicate,
                                             this.attributes, this.immuneTempModifiers, this.hideIfUnmet);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
