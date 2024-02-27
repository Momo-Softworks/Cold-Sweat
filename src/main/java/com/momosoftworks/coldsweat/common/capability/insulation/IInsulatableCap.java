package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IInsulatableCap extends INBTSerializable<CompoundTag>
{
    List<Pair<ItemStack, List<Insulation>>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);
}
