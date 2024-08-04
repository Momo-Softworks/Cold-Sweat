package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.List;

public interface IInsulatableCap
{
    List<Pair<ItemStack, List<Insulation>>> getInsulation();
    boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem);

    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);

    void copy(IInsulatableCap cap);

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT tag);
}
