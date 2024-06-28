package com.momosoftworks.coldsweat.util.serialization;

import net.minecraft.nbt.CompoundTag;

public interface NbtDeserializable
{
    void deserialize(CompoundTag nbt);
}
