package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ItemInsulationCap implements IInsulatableCap
{
    private List<Pair<ItemStack, List<Insulation>>> insulation = new ArrayList<>();

    int saveCooldown = 0;
    CompoundTag savedTag = new CompoundTag();

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
                                                                             .toList())).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
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
                if (pair instanceof AdaptiveInsulation insul)
                {
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
            this.insulation.add(Pair.of(stack, new ArrayList<>(List.of(insulation))));
            return insulation;
        });
        this.calculateInsulation();
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, List<Insulation>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        if (toRemove.isPresent())
        {   this.insulation.remove(toRemove.get());
            this.calculateInsulation();
        }
        return stack;
    }

    public ItemStack getInsulationItem(int index)
    {   return this.insulation.stream().map(Pair::getFirst).skip(index).findFirst().orElse(ItemStack.EMPTY);
    }

    public List<Insulation> getInsulationValues()
    {   return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        // For some reason Forge runs this nearly 400 times a second, so we cache the tag
        if (saveCooldown > 0)
        {   saveCooldown--;
            return savedTag;
        }

        // Save the insulation items
        ListTag insulNBT = new ListTag();
        // Iterate over insulation items
        for (Pair<ItemStack, List<Insulation>> entry : insulation)
        {
            CompoundTag entryNBT = new CompoundTag();
            List<Insulation> pairList = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundTag()));

            ListTag pairListNBT = serializeInsulation(pairList);
            entryNBT.put("Values", pairListNBT);
            // Add the item to the list
            insulNBT.add(entryNBT);
        }

        CompoundTag tag = new CompoundTag();
        tag.put("Insulation", insulNBT);

        saveCooldown = 400;
        savedTag = tag;
        return tag;
    }

    private static ListTag serializeInsulation(List<Insulation> pairList)
    {
        ListTag pairListNBT = new ListTag();
        // Store insulation values for the item
        for (Insulation pair : pairList)
        {
            CompoundTag pairTag = new CompoundTag();
            if (pair instanceof StaticInsulation insul)
            {
                pairTag.putDouble("Cold", insul.getCold());
                pairTag.putDouble("Hot", insul.getHot());
            }
            else if (pair instanceof AdaptiveInsulation insul)
            {
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
    public void deserializeNBT(CompoundTag tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListTag insulList = tag.getList("Insulation", 10);

        // Iterate over insulation items
        for (int i = 0; i < insulList.size(); i++)
        {
            CompoundTag insulNBT = insulList.getCompound(i);
            ItemStack insulItem = ItemStack.of(insulNBT.getCompound("Item"));
            List<Insulation> pairList = new ArrayList<>();
            ListTag pairListNBT = insulNBT.getList("Values", 10);

            // Iterate over insulation values for the item
            for (int j = 0; j < pairListNBT.size(); j++)
            {
                CompoundTag pairTag = pairListNBT.getCompound(j);
                if (pairTag.contains("Cold"))
                {   double cold = pairTag.getDouble("Cold");
                    double hot = pairTag.getDouble("Hot");
                    pairList.add(new StaticInsulation(cold, hot));
                }
                else if (pairTag.contains("Amount"))
                {   double insul = pairTag.getDouble("Amount");
                    double factor = pairTag.getDouble("Factor");
                    double speed = pairTag.getDouble("Speed");
                    pairList.add(new AdaptiveInsulation(insul, factor, speed));
                }
            }
            // Add the item to the map
            insulation.add(Pair.of(insulItem, pairList));
        }
        this.calculateInsulation();
    }

    public CompoundTag serializeSimple(ItemStack stack)
    {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag insulTag = new ListTag();

        for (List<Insulation> pairList : this.insulation.stream().map(Pair::getSecond).toList())
        {
            for (Insulation pair : pairList)
            {
                CompoundTag compound = new CompoundTag();
                if (pair instanceof StaticInsulation insul)
                {
                    compound.putDouble("Cold", insul.getCold());
                    compound.putDouble("Hot", insul.getHot());
                }
                else if (pair instanceof AdaptiveInsulation insul)
                {
                    compound.putDouble("Insulation", insul.getInsulation());
                    compound.putDouble("Factor", insul.getFactor());
                    compound.putDouble("Speed", insul.getSpeed());
                }
                insulTag.add(compound);
            }
        }
        tag.put("SimpleInsulation", insulTag);

        return tag;
    }

    public List<Insulation> deserializeSimple(ItemStack stack)
    {
        List<Insulation> pairs = stack.getOrCreateTag().getList("SimpleInsulation", 10).stream()
        .map(nbt ->
        {
            CompoundTag compound = (CompoundTag) nbt;

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
