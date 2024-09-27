package com.momosoftworks.coldsweat.config.type;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class Insulator implements NbtSerializable
{
    public final Insulation insulation;
    public final Insulation.Slot slot;
    public final ItemRequirement data;
    public final EntityRequirement predicate;
    public final AttributeModifierMap attributes;
    public final Map<ResourceLocation, Double> immuneTempModifiers;

    public Insulator(Insulation insulation, Insulation.Slot slot, ItemRequirement data,
                     EntityRequirement predicate, AttributeModifierMap attributes,
                     Map<ResourceLocation, Double> immuneTempModifiers)
    {
        this.insulation = insulation;
        this.slot = slot;
        this.data = data;
        this.predicate = predicate;
        this.attributes = attributes;
        this.immuneTempModifiers = immuneTempModifiers;
    }
    public boolean test(Entity entity, ItemStack stack)
    {   return predicate.test(entity) && data.test(stack, true);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("insulation", insulation.serialize());
        tag.put("slot", Insulation.Slot.CODEC.encodeStart(NBTDynamicOps.INSTANCE, slot).result().get());
        tag.put("data", data.serialize());
        if (!predicate.equals(EntityRequirement.NONE)) tag.put("predicate", predicate.serialize());
        if (!attributes.isEmpty()) tag.put("attributes", attributes.serialize());
        if (!immuneTempModifiers.isEmpty())
        {
            CompoundNBT immuneTempModifiersTag = new CompoundNBT();
            immuneTempModifiers.forEach((key, value) -> immuneTempModifiersTag.putDouble(key.toString(), value));
            tag.put("immune_temp_modifiers", immuneTempModifiersTag);
        }

        return tag;
    }

    public static Insulator deserialize(CompoundNBT tag)
    {
        Insulation insulation = Insulation.deserialize(tag.getCompound("insulation"));
        Insulation.Slot slot = Insulation.Slot.CODEC.parse(NBTDynamicOps.INSTANCE, tag.get("slot")).result().get();
        ItemRequirement data = ItemRequirement.deserialize(tag.getCompound("data"));
        EntityRequirement predicate = EntityRequirement.deserialize(tag.getCompound("predicate"));
        AttributeModifierMap attributes = AttributeModifierMap.deserialize(tag.getCompound("attributes"));
        CompoundNBT immuneTempModifiersTag = tag.getCompound("immune_temp_modifiers");
        Map<ResourceLocation, Double> immuneTempModifiers = new HashMap<>();
        immuneTempModifiersTag.getAllKeys().forEach(key -> immuneTempModifiers.put(new ResourceLocation(key), immuneTempModifiersTag.getDouble(key)));

        return new Insulator(insulation, slot, data, predicate, attributes, immuneTempModifiers);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Insulator insulator = (Insulator) obj;

        return insulation.equals(insulator.insulation)
            && data.equals(insulator.data)
            && predicate.equals(insulator.predicate)
            && attributes.equals(insulator.attributes);
    }
}
