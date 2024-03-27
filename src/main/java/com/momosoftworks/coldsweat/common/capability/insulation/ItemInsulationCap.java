package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, List<Insulation>>> insulation = new ArrayList<>();

    @Override
    public List<Pair<ItemStack, List<Insulation>>> getInsulation()
    {   return this.insulation;
    }

    public void calcAdaptiveInsulation(double worldTemp, double minShift, double maxShift)
    {
        double minTemp = ConfigSettings.MIN_TEMP.get() + minShift;
        double maxTemp = ConfigSettings.MAX_TEMP.get() + maxShift;

        for (Pair<ItemStack, List<Insulation>> entry : insulation)
        {
            List<Insulation> list = entry.getSecond();
            for (Insulation pair : list)
            {
                if (pair instanceof AdaptiveInsulation)
                {
                    AdaptiveInsulation insul = (AdaptiveInsulation) pair;
                    double factor = insul.getFactor();
                    double adaptSpeed = insul.getSpeed();

                    double newFactor;
                    if (CSMath.betweenInclusive(CSMath.blend(-1, 1, worldTemp, minTemp, maxTemp), -0.25, 0.25))
                    {   newFactor = CSMath.shrink(factor, adaptSpeed);
                    }
                    else
                    {   newFactor = CSMath.clamp(factor + CSMath.blend(-adaptSpeed, adaptSpeed, worldTemp, minTemp, maxTemp), -1, 1);
                    }
                    insul.setFactor(newFactor);
                }
            }
        }
    }

    public void addInsulationItem(ItemStack stack)
    {
        ConfigSettings.INSULATION_ITEMS.get().computeIfPresent(stack.getItem(), (item, insulator) ->
        {
            this.insulation.add(Pair.of(stack, insulator.insulation.split()));
            return insulator;
        });
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, List<Insulation>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        toRemove.ifPresent(this.insulation::remove);
        return stack;
    }

    public ItemStack getInsulationItem(int index)
    {   return this.insulation.get(index).getFirst();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        // Save the insulation items
        ListNBT insulNBT = new ListNBT();
        // Iterate over insulation items
        for (Pair<ItemStack, List<Insulation>> entry : insulation)
        {
            CompoundNBT entryNBT = new CompoundNBT();
            List<Insulation> pairList = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundNBT()));

            ListNBT pairListNBT = serializeInsulation(pairList);
            entryNBT.put("Values", pairListNBT);
            // Add the item to the list
            insulNBT.add(entryNBT);
        }

        CompoundNBT tag = new CompoundNBT();
        tag.put("Insulation", insulNBT);

        return tag;
    }

    private static ListNBT serializeInsulation(List<Insulation> pairList)
    {
        ListNBT insulList = new ListNBT();
        // Store insulation values for the item
        for (Insulation insulation : pairList)
        {
            insulList.add(insulation.serialize());
        }
        return insulList;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListNBT insulList = tag.getList("Insulation", 10);

        // Iterate over insulation items
        for (int i = 0; i < insulList.size(); i++)
        {
            CompoundNBT entryNBT = insulList.getCompound(i);
            ItemStack stack = ItemStack.of(entryNBT.getCompound("Item"));
            List<Insulation> pairList = entryNBT.getList("Values", 10).stream().map(tg -> Insulation.deserialize(((CompoundNBT) tg)))
                                                                      .filter(Objects::nonNull)
                                                                      .map(Insulation::split)
                                                                      .flatMap(Collection::stream)
                                                                      .collect(Collectors.toList());
            this.insulation.add(Pair.of(stack, new ArrayList<>(pairList)));
        }
    }
}
