package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
        List<Item> itemList = RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, ConfigHelper.getItems(items));
        if (itemList.isEmpty() && items.length != 0)
        {   this.itemPredicate.add(ItemRequirement.NONE, true);
        }
        else
        {   this.itemPredicate.add(new ItemRequirement(itemList, null), false);
        }
        return this;
    }

    public ItemTempBuilderJS temperature(Function<Map<String, Object>, Double> getter)
    {   this.temperature = ValueGetter.of(getter);
        return this;
    }
    public ItemTempBuilderJS temperature(double temperature)
    {   this.temperature = ValueGetter.constant(temperature);
        return this;
    }

    public ItemTempBuilderJS maxEffect(Function<Map<String, Object>, Double> getter)
    {   this.maxEffect = ValueGetter.of(getter);
        return this;
    }
    public ItemTempBuilderJS maxEffect(double maxEffect)
    {   this.maxEffect = ValueGetter.constant(maxEffect);
        return this;
    }

    public ItemTempBuilderJS maxTemp(Function<Map<String, Object>, Double> getter)
    {   this.maxTemp = ValueGetter.of(getter);
        return this;
    }
    public ItemTempBuilderJS maxTemp(double maxTemp)
    {   this.maxTemp = ValueGetter.constant(maxTemp);
        return this;
    }

    public ItemTempBuilderJS minTemp(Function<Map<String, Object>, Double> getter)
    {   this.minTemp = ValueGetter.of(getter);
        return this;
    }
    public ItemTempBuilderJS minTemp(double minTemp)
    {   this.minTemp = ValueGetter.constant(minTemp);
        return this;
    }

    public ItemTempBuilderJS trait(String trait)
    {   this.trait = Temperature.Trait.fromID(trait);
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
    {   this.slots.add(Either.left(new IntegerBounds(min, max)));
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
    {   this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public ItemTempBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {   this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public ItemTempBuilderJS attribute(String attributeId, double amount, String operation)
    {
        Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(attributeId)).orElse(null);
        if (!KubeHelper.expect(attributeId, attribute, Holder.class))
        {   return this;
        }
        attributes.put(attribute, new AttributeModifier(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "kubejs"), amount,
                                                        AttributeModifier.Operation.valueOf(operation.toUpperCase(Locale.ROOT))));
        return this;
    }

    protected ItemTempBuilderJS immuneToModifier(String modifierId, ValueGetter<Double> immunity)
    {
        ResourceLocation location = ResourceLocation.parse(modifierId);
        if (!TempModifierRegistry.getEntries().containsKey(location))
        {   ColdSweat.LOGGER.warn("Tried to add immunity to non-existent temperature modifier: {}", location);
            return this;
        }
        immuneTempModifiers.put(ResourceLocation.parse(modifierId), immunity);
        return this;
    }
    public ItemTempBuilderJS immuneToModifier(String modifierId, Function<Map<String, Object>, Double> immunity)
    {   return immuneToModifier(modifierId, ValueGetter.of(immunity));
    }
    public ItemTempBuilderJS immuneToModifier(String modifierId, double immunity)
    {   return immuneToModifier(modifierId, ValueGetter.constant(immunity));
    }

    public ItemTempBuilderJS hideIfUnmet(Function<Map<String, Object>, Boolean> hideIfUnmet)
    {   this.hideIfUnmet = ValueGetter.of(hideIfUnmet);
        return this;
    }
    public ItemTempBuilderJS hideIfUnmet(boolean hideIfUnmet)
    {   this.hideIfUnmet = ValueGetter.constant(hideIfUnmet);
        return this;
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
