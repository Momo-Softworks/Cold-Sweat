package com.momosoftworks.coldsweat.common.capability;

import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Bridges CompoundTag-based serialization to {@link ValueIOSerializable}, which replaced {@code INBTSerializable}.<br>
 * The tag's entries are written directly into the output, so the saved format is unchanged.
 */
public interface CompoundSerializable extends ValueIOSerializable
{
    CompoundTag serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt);

    @Override
    default void serialize(ValueOutput output)
    {   output.store(NBTHelper.COMPOUND_MAP_CODEC, this.serializeNBT(RegistryHelper.getRegistryAccess()));
    }

    @Override
    default void deserialize(ValueInput input)
    {   this.deserializeNBT(input.lookup(), input.read(NBTHelper.COMPOUND_MAP_CODEC).orElseGet(CompoundTag::new));
    }
}
