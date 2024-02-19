package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

public interface IInsulatableCap
{
    List<Pair<ItemStack, List<ItemInsulationCap.InsulationPair>>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT tag);
}
