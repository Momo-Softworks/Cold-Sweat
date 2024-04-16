package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;


public record PredicateItem(Double value, ItemRequirement data, EntityRequirement requirement) implements NbtSerializable
{
    public boolean test(ItemStack stack)
    {   return data.test(stack, true);
    }

    public boolean test(Entity entity, ItemStack stack)
    {   return data.test(stack, true) && requirement.test(entity);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("value", value);
        tag.put("data", data.serialize());
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static PredicateItem deserialize(CompoundTag tag)
    {
        return new PredicateItem(tag.getDouble("value"),
                                 ItemRequirement.deserialize(tag.getCompound("data")),
                                 EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
