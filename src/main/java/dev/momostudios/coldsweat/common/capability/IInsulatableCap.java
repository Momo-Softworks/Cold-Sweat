package dev.momostudios.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IInsulatableCap
{
    List<ItemStack> getInsulationItems();
    List<Pair<Double, Double>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(int index);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
