package net.momostudios.coldsweat.core.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.momostudios.coldsweat.util.PlayerHelper;

import javax.annotation.Nullable;

public class PlayerTempCapStorage implements Capability.IStorage<PlayerTempCapability> {
    @Override
    @Nullable
    public INBT writeNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction direction)
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble("ambient", instance.get(PlayerHelper.Types.AMBIENT));
        nbt.putDouble("body", instance.get(PlayerHelper.Types.BODY));
        nbt.putDouble("base", instance.get(PlayerHelper.Types.BASE));
        nbt.putDouble("composite", instance.get(PlayerHelper.Types.COMPOSITE));
        return nbt;
    }

    @Override
    public void readNBT(Capability<PlayerTempCapability> capability, PlayerTempCapability instance, Direction direction, INBT nbtData) {
        instance.set(PlayerHelper.Types.AMBIENT, ((CompoundNBT) nbtData).getDouble("ambient"));
        instance.set(PlayerHelper.Types.BODY, ((CompoundNBT) nbtData).getDouble("body"));
        instance.set(PlayerHelper.Types.BASE, ((CompoundNBT) nbtData).getDouble("base"));
        instance.set(PlayerHelper.Types.COMPOSITE, ((CompoundNBT) nbtData).getDouble("composite"));
    }
}
