package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;


public record ItemValue(Double value, NbtRequirement nbt) implements NbtSerializable
{
    public boolean test(ItemStack stack)
    {
        return nbt.test(stack);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("value", value);
        tag.put("nbt", nbt.serialize());
        return tag;
    }

    public static ItemValue deserialize(CompoundTag tag)
    {
        return new ItemValue(tag.getDouble("value"), NbtRequirement.deserialize(tag.getCompound("nbt")));
    }
}
