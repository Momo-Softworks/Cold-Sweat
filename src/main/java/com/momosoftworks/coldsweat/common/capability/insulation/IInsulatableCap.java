package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Collection;
import java.util.List;

public interface IInsulatableCap
{
    List<Pair<ItemStack, List<InsulatorData>>> getInsulation();
    List<InsulatorData> getInsulators();
    boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem);

    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);

    void copy(IInsulatableCap cap);

    CompoundNBT serializeNBT();
    void deserializeNBT(CompoundNBT tag);
}
