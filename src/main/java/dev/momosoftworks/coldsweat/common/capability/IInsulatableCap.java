package dev.momosoftworks.coldsweat.common.capability;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

public interface IInsulatableCap extends INBTSerializable<CompoundTag>
{
    List<Pair<ItemStack, List<ItemInsulationCap.InsulationPair>>> getInsulation();
    void addInsulationItem(ItemStack stack);
    ItemStack removeInsulationItem(ItemStack stack);
    ItemStack getInsulationItem(int index);
}
