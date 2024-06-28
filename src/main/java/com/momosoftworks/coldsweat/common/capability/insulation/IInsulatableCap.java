package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.util.serialization.NbtDeserializable;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IInsulatableCap extends NbtSerializable, NbtDeserializable
{
    List<Pair<ItemStack, List<Insulation>>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);
}
