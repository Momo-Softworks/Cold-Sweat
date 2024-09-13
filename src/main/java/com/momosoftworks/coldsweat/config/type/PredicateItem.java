package com.momosoftworks.coldsweat.config.type;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;


public record PredicateItem(Double value, ItemRequirement data, EntityRequirement requirement, CompoundTag extraData) implements NbtSerializable
{
    public PredicateItem(Double value, ItemRequirement data, EntityRequirement requirement)
    {   this(value, data, requirement, new CompoundTag());
    }

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
        if (extraData != null && !extraData.isEmpty())
        {   tag.put("extraData", extraData);
        }
        return tag;
    }

    public static PredicateItem deserialize(CompoundTag tag)
    {
        return new PredicateItem(tag.getDouble("value"),
                                 ItemRequirement.deserialize(tag.getCompound("data")),
                                 EntityRequirement.deserialize(tag.getCompound("requirement")),
                                 tag.contains("extraData") ? tag.getCompound("extraData") : new CompoundTag());
    }
}
