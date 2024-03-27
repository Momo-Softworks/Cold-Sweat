package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;


public class ItemValue implements NbtSerializable
{
    public final Double value;
    public final NbtRequirement nbt;
    
    public ItemValue(Double value, NbtRequirement nbt)
    {
        this.value = value;
        this.nbt = nbt;
    }
    public boolean test(ItemStack stack)
    {
        return nbt.test(stack);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putDouble("value", value);
        tag.put("nbt", nbt.serialize());
        return tag;
    }

    public static ItemValue deserialize(CompoundNBT tag)
    {
        return new ItemValue(tag.getDouble("value"), NbtRequirement.deserialize(tag.getCompound("nbt")));
    }
}
