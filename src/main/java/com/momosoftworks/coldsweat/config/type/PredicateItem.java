package com.momosoftworks.coldsweat.config.type;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;



public class PredicateItem implements NbtSerializable
{
    public Double value;
    public ItemRequirement data;
    public EntityRequirement requirement;

    public PredicateItem(Double value, ItemRequirement data, EntityRequirement requirement)
    {
        this.value = value;
        this.data = data;
        this.requirement = requirement;
    }

    public boolean test(ItemStack stack)
    {   return data.test(stack, true);
    }

    public boolean test(Entity entity, ItemStack stack)
    {   return data.test(stack, true) && requirement.test(entity);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("value", value);
        tag.put("data", data.serialize());
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static PredicateItem deserialize(CompoundNBT tag)
    {
        return new PredicateItem(tag.getDouble("value"),
                                 ItemRequirement.deserialize(tag.getCompound("data")),
                                 EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
