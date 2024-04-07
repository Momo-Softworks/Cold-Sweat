package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;



public class PredicateItem implements NbtSerializable
{
    public Double value;
    public NbtRequirement nbt;
    public EntityRequirement requirement;
    
    public PredicateItem(Double value, NbtRequirement nbt, EntityRequirement requirement)
    {
        this.value = value;
        this.nbt = nbt;
        this.requirement = requirement;
    }
    public boolean test(ItemStack stack)
    {   return nbt.test(stack) ;
    }

    public boolean test(Entity entity, ItemStack stack)
    {   return nbt.test(stack) && requirement.test(entity);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("value", value);
        tag.put("nbt", nbt.serialize());
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static PredicateItem deserialize(CompoundNBT tag)
    {
        return new PredicateItem(tag.getDouble("value"), NbtRequirement.deserialize(tag.getCompound("nbt")), EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
