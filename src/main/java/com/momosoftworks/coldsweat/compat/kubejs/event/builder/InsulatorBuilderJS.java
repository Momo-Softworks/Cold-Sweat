package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.compat.kubejs.util.KubeHelper;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
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

public class InsulatorBuilderJS
{
    public List<Insulation> insulation = new ArrayList<>();
    public Insulation.Slot slot = null;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public AttributeModifierMap attributes = new AttributeModifierMap();
    public Map<ResourceLocation, Double> immuneTempModifiers = new HashMap<>();
    public ValueGetter<Boolean> fillSlots = ValueGetter.constant(true);
    public ValueGetter<Boolean> hideIfUnmet = ValueGetter.constant(false);
    public String hintKey = null;
    public String hintText = null;

    public InsulatorBuilderJS()
    {}

    public InsulatorBuilderJS items(String... items)
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

    public InsulatorBuilderJS insulation(double cold, double heat)
    {
        this.insulation.add(new StaticInsulation(cold, heat));
        return this;
    }

    public InsulatorBuilderJS adaptiveInsulation(double insulation, double speed)
    {
        this.insulation.add(new AdaptiveInsulation(insulation, speed));
        return this;
    }

    public InsulatorBuilderJS slot(String slot)
    {
        this.slot = Insulation.Slot.byName(slot);
        return this;
    }

    public InsulatorBuilderJS itemPredicate(Predicate<ItemStack> itemPredicate)
    {
        this.itemPredicate.add(new ItemRequirement(itemPredicate), false);
        return this;
    }

    public InsulatorBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public InsulatorBuilderJS attribute(String attributeId, double amount, String operation)
    {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeId));
        if (!KubeHelper.expect(attributeId, attribute, Attribute.class))
        {   return this;
        }
        attributes.put(attribute, new AttributeModifier("kubejs", amount, AttributeModifier.Operation.valueOf(operation.toUpperCase(Locale.ROOT))));
        return this;
    }

    public InsulatorBuilderJS immuneToModifier(String modifierId, double immunity)
    {
        ResourceLocation location = new ResourceLocation(modifierId);
        if (!TempModifierRegistry.getEntries().containsKey(location))
        {
            ColdSweat.LOGGER.warn("Tried to add immunity to non-existent temperature modifier: {}", location);
            return this;
        }
        immuneTempModifiers.put(new ResourceLocation(modifierId), immunity);
        return this;
    }

    public InsulatorBuilderJS fillSlots(Function<Map<String, Object>, Boolean> function)
    {
        this.fillSlots = ValueGetter.of(function);
        return this;
    }
    public InsulatorBuilderJS fillSlots(boolean multiSlot)
    {   return this.fillSlots(m -> multiSlot);
    }

    public InsulatorBuilderJS hideIfUnmet(Function<Map<String, Object>, Boolean> function)
    {
        this.hideIfUnmet = ValueGetter.of(function);
        return this;
    }
    public InsulatorBuilderJS hideIfUnmet(boolean hideIfUnmet)
    {   return this.hideIfUnmet(m -> hideIfUnmet);
    }

    public InsulatorBuilderJS hintKey(String key)
    {
        this.hintKey = key;
        return this;
    }

    public InsulatorBuilderJS hintText(String text)
    {
        this.hintText = text;
        return this;
    }

    public InsulatorData build()
    {
        Optional<InsulatorData.HintText> hint = hintKey != null || hintText != null
                                                ? Optional.of(new InsulatorData.HintText(Optional.ofNullable(hintKey), Optional.ofNullable(hintText)))
                                                : Optional.empty();
        InsulatorData data = new InsulatorData(this.itemPredicate, this.slot, this.insulation, this.entityPredicate,
                                               this.attributes, this.immuneTempModifiers, this.fillSlots, this.hideIfUnmet, hint);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
