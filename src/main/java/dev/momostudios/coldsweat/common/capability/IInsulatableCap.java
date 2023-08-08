package dev.momostudios.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Triplet;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IInsulatableCap
{
    List<Pair<ItemStack, List<ItemInsulationCap.InsulationPair>>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
