package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;


public record PredicateItem(Double value, NbtRequirement nbt, EntityRequirement requirement) implements NbtSerializable
{
    public boolean test(ItemStack stack)
    {   return nbt.test(stack) ;
    }

    public boolean test(Entity entity, ItemStack stack)
    {   return nbt.test(stack) && requirement.test(entity);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("value", value);
        tag.put("nbt", nbt.serialize());
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static PredicateItem deserialize(CompoundTag tag)
    {
        return new PredicateItem(tag.getDouble("value"), NbtRequirement.deserialize(tag.getCompound("nbt")), EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
