package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public record Insulator(Insulation insulation, InsulationSlot slot, NbtRequirement nbt,
                        EntityRequirement predicate, AttributeModifierMap attributes) implements NbtSerializable
{
    public boolean test(Entity entity, ItemStack stack)
    {
        return predicate.test(entity) && nbt.test(stack);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.put("insulation", insulation.serialize());
        tag.put("slot", InsulationSlot.CODEC.encodeStart(NbtOps.INSTANCE, slot).result().get());
        tag.put("nbt", nbt.serialize());
        tag.put("predicate", predicate.serialize());
        tag.put("attributes", attributes.serialize());
        return tag;
    }

    public static Insulator deserialize(CompoundTag tag)
    {
        Insulation insulation = Insulation.deserialize(tag.getCompound("insulation"));
        InsulationSlot slot = InsulationSlot.CODEC.parse(NbtOps.INSTANCE, tag.get("slot")).result().get();
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
