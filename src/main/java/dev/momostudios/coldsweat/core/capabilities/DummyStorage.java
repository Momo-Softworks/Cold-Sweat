package dev.momostudios.coldsweat.core.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DummyStorage
{
    @Nullable
    public Tag writeNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction side)
    {
        return new CompoundTag();
    }

    public void readNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction side, Tag nbt)
    {
    }
}
