package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, List<Insulation>>> insulation = new ArrayList<>();

    @Override
    public List<Pair<ItemStack, List<Insulation>>> getInsulation()
    {   return this.insulation;
    }

    public void calcAdaptiveInsulation(double worldTemp, double minTemp, double maxTemp)
    {
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

    public boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem)
    {
        AtomicInteger positiveInsul = new AtomicInteger();

        Insulator insulator = ConfigSettings.INSULATION_ITEMS.get().get(insulationItem.getItem());
        if (insulator == null)
        {   return false;
        }

        List<Pair<ItemStack, List<Insulation>>> insulList = new ArrayList<>(this.insulation);
        insulList.add(Pair.of(insulationItem, insulator.insulation.split()));

        // Get the total positive/negative insulation of the armor
        insulList.stream().map(Pair::getSecond).flatMap(Collection::stream).forEach(insul ->
        {
            if (insul.getHeat() >= 0 || insul.getCold() >= 0)
            {   positiveInsul.getAndIncrement();
            }
        });
        return positiveInsul.get() <= ItemInsulationManager.getInsulationSlots(armorItem);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        // Save the insulation items
        ListNBT insulNBT = new ListNBT();
        // Iterate over insulation items
        for (int i = 0; i < insulation.size(); i++)
        {
            Pair<ItemStack, List<Insulation>> entry = insulation.get(i);
            CompoundNBT entryNBT = new CompoundNBT();
            List<Insulation> pairList = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundNBT()));
            // Store insulation data
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
        for (int i = 0; i < pairList.size(); i++)
        {   insulList.add(pairList.get(i).serialize());
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

    @Override
    public void copy(IInsulatableCap cap)
    {
        this.insulation.clear();
        this.insulation.addAll(cap.getInsulation());
    }
}
