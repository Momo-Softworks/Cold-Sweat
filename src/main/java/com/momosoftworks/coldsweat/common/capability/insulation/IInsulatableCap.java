package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.List;

public interface IInsulatableCap extends INBTSerializable<CompoundTag>
{
    List<Pair<ItemStack, Collection<InsulatorData>>> getInsulation();
    List<InsulatorData> getInsulators();
    boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem);

    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);

    void copy(IInsulatableCap cap);
}
