package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;


public class Insulator implements NbtSerializable
{
    public Insulation insulation;
    public InsulationSlot slot;
    public NbtRequirement nbt;
    public EntityRequirement predicate;
    public AttributeModifierMap attributes;
    
    public Insulator(Insulation insulation, InsulationSlot slot, NbtRequirement nbt,
                     EntityRequirement predicate, AttributeModifierMap attributes)
    {
        this.insulation = insulation;
        this.slot = slot;
        this.nbt = nbt;
        this.predicate = predicate;
        this.attributes = attributes;
    }
    public boolean test(Entity entity, ItemStack stack)
    {
        return predicate.test(entity) && nbt.test(stack);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("insulation", insulation.serialize());
        tag.put("slot", InsulationSlot.CODEC.encodeStart(NBTDynamicOps.INSTANCE, slot).result().get());
        tag.put("nbt", nbt.serialize());
        tag.put("predicate", predicate.serialize());
        tag.put("attributes", attributes.serialize());
        return tag;
    }

    public static Insulator deserialize(CompoundNBT tag)
    {
        Insulation insulation = Insulation.deserialize(tag.getCompound("insulation"));
        InsulationSlot slot = InsulationSlot.CODEC.parse(NBTDynamicOps.INSTANCE, tag.get("slot")).result().get();
        NbtRequirement nbt = NbtRequirement.deserialize(tag.getCompound("nbt"));
        EntityRequirement predicate = EntityRequirement.deserialize(tag.getCompound("predicate"));
        AttributeModifierMap attributes = AttributeModifierMap.deserialize(tag.getCompound("attributes"));
        return new Insulator(insulation, slot, nbt, predicate, attributes);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Insulator insulator = (Insulator) obj;
        return insulation.equals(insulator.insulation)
            && nbt.equals(insulator.nbt)
            && predicate.equals(insulator.predicate)
            && attributes.equals(insulator.attributes);
    }
}
