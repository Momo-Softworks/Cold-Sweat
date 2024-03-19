package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private List<Pair<ItemStack, List<Insulation>>> insulation = new ArrayList<>();

    int saveCooldown = 0;
    CompoundNBT savedTag = new CompoundNBT();

    @Override
    public List<Pair<ItemStack, List<Insulation>>> getInsulation()
    {
        return this.insulation;
    }

    // Sorts the items and insulation values based on their temperatures
    void calculateInsulation()
    {
        // Iterate through insulation items and tally up cold, hot, and neutral insulation
        this.insulation = this.insulation.stream().map(insulation -> Pair.of(insulation.getFirst(),
                                                                             insulation.getSecond().stream()
                                                                             .map(Insulation::split)
                                                                             .flatMap(Collection::stream)
                                                                             .collect(Collectors.toList()))).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        saveCooldown = 0;
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
        ConfigSettings.INSULATION_ITEMS.get().computeIfPresent(ItemData.of(stack), (item, insulation) ->
        {
            this.insulation.add(Pair.of(stack, new ArrayList<>(Arrays.asList(insulation))));
            return insulation;
        });
        this.calculateInsulation();
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, List<Insulation>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        if (toRemove.isPresent())
        {
            this.insulation.remove(toRemove.get());
            this.calculateInsulation();
        }
        return stack;
    }

    public ItemStack getInsulationItem(int index)
    {
        return this.insulation.stream().map(Pair::getFirst).skip(index).findFirst().orElse(ItemStack.EMPTY);
    }

    public List<Insulation> getInsulationValues()
    {   return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        // For some reason Forge runs this every tick,
        // so we store the last-saved tag and return it
        if (saveCooldown > 0)
        {
            saveCooldown--;
            return savedTag;
        }

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

        saveCooldown = 400;
        savedTag = tag;
        return tag;
    }

    private static ListNBT serializeInsulation(List<Insulation> pairList)
    {
        ListNBT pairListNBT = new ListNBT();
        // Store insulation values for the item
        for (Insulation pair : pairList)
        {
            CompoundNBT pairTag = new CompoundNBT();
            if (pair instanceof StaticInsulation)
            {
                StaticInsulation insul = ((StaticInsulation) pair);
                pairTag.putDouble("Cold", insul.getCold());
                pairTag.putDouble("Hot", insul.getHot());
            }
            else if (pair instanceof AdaptiveInsulation)
            {
                AdaptiveInsulation insul = ((AdaptiveInsulation) pair);
                pairTag.putDouble("Amount", insul.getInsulation());
                pairTag.putDouble("Factor", insul.getFactor());
                pairTag.putDouble("Speed", insul.getSpeed());
            }
            // Add the value to the pair list
            pairListNBT.add(pairTag);
        }
        return pairListNBT;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListNBT insulNBT = tag.getList("Insulation", 10);

        // Iterate over insulation items
        for (int i = 0; i < insulNBT.size(); i++)
        {
            CompoundNBT entryNBT = insulNBT.getCompound(i);
            ItemStack stack = ItemStack.of(entryNBT.getCompound("Item"));
            List<Insulation> pairList = new ArrayList<>();
            ListNBT pairListNBT = entryNBT.getList("Values", 10);

            // Iterate over insulation values for the item
            for (int j = 0; j < pairListNBT.size(); j++)
            {
                CompoundNBT pairTag = pairListNBT.getCompound(j);
                if (pairTag.contains("Cold"))
                {
                    double cold = pairTag.getDouble("Cold");
                    double hot = pairTag.getDouble("Hot");
                    pairList.add(new StaticInsulation(cold, hot));
                }
                else if (pairTag.contains("Amount"))
                {
                    double insul = pairTag.getDouble("Amount");
                    double factor = pairTag.getDouble("Factor");
                    double speed = pairTag.getDouble("Speed");
                    pairList.add(new AdaptiveInsulation(insul, factor, speed));
                }
            }

            // Add the item to the map
            this.insulation.add(Pair.of(stack, pairList));
        }
        this.calculateInsulation();
    }

    public CompoundNBT serializeSimple(ItemStack stack)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        ListNBT insulTag = new ListNBT();

        for (List<Insulation> pairList : this.insulation.stream().map(Pair::getSecond).collect(Collectors.toList()))
        {
            for (Insulation pair : pairList)
            {
                CompoundNBT compound = new CompoundNBT();
                if (pair instanceof StaticInsulation)
                {
                    StaticInsulation insul = ((StaticInsulation) pair);
                    compound.putDouble("Cold", insul.getCold());
                    compound.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation)
                {   AdaptiveInsulation insul = (AdaptiveInsulation) pair;
                    compound.putDouble("Insulation", insul.getInsulation());
                    compound.putDouble("Factor", insul.getFactor());
                    compound.putDouble("Speed", insul.getSpeed());
                }
                insulTag.add(compound);
            }
        }
        if (!insulTag.isEmpty())
            tag.put("SimpleInsulation", insulTag);
        else
            tag.remove("SimpleInsulation");

        return tag;
    }

    public List<Insulation> deserializeSimple(ItemStack stack)
    {
        List<Insulation> pairs = stack.getOrCreateTag().getList("SimpleInsulation", 10).stream()
        .map(nbt ->
        {
            CompoundNBT compound = (CompoundNBT) nbt;

            if (compound.contains("Cold"))
            {   return new StaticInsulation(compound.getDouble("Cold"), compound.getDouble("Hot"));
            }
            else
            {   return new AdaptiveInsulation(compound.getDouble("Insulation"), compound.getDouble("Factor"), compound.getDouble("Speed"));
            }
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        return Insulation.sort(pairs);
    }
}
