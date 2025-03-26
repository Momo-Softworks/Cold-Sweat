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
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InsulatorBuilderJS
{
    public List<Insulation> insulation = new ArrayList<>();
    public Insulation.Slot slot = null;
    public NegatableList<ItemRequirement> itemPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public AttributeModifierMap attributes = new AttributeModifierMap();
    public Map<ResourceLocation, Double> immuneTempModifiers = new HashMap<>();
    public boolean multiSlot = false;

    public InsulatorBuilderJS()
    {}

    public InsulatorBuilderJS items(String... items)
    {
        List<Item> itemList = RegistryHelper.mapTaggableList(ConfigHelper.getItems(items));
        this.itemPredicate.add(new ItemRequirement(itemList, null), false);
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

    public InsulatorBuilderJS multiSlot(boolean multiSlot)
    {
        this.multiSlot = multiSlot;
        return this;
    }

    public InsulatorData build()
    {
        InsulatorData data = new InsulatorData(this.itemPredicate, slot, insulation, this.entityPredicate,
                                               attributes, immuneTempModifiers, multiSlot);
        data.setType(ConfigData.Type.KUBEJS);
        return data;
    }
}
