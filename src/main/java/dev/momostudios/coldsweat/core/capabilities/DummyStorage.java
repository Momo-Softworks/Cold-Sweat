package dev.momostudios.coldsweat.core.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class DummyStorage implements Capability.IStorage<PlayerTempCapability>
{
    @Nullable
    @Override
    public INBT writeNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction side)
    {
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction side, INBT nbt)
    {
    }
}
