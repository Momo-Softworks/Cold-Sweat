package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, Collection<InsulatorData>>> insulation = new ArrayList<>();
    private boolean changed = false;
    private CompoundNBT oldSerialized = null;

    @Override
    public List<Pair<ItemStack, Collection<InsulatorData>>> getInsulation()
    {   return this.insulation;
    }

    public List<InsulatorData> getInsulators()
    {   return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public void calcAdaptiveInsulation(double worldTemp, double minTemp, double maxTemp)
    {
        for (Pair<ItemStack, Collection<InsulatorData>> entry : insulation)
        {
            for (InsulatorData insulatorData : entry.getSecond())
            {
                List<Insulation> entryDataList = insulatorData.insulation();
                for (int i = 0; i < entryDataList.size(); i++)
                {
                    Insulation entryInsul = entryDataList.get(i);
                    if (entryInsul instanceof AdaptiveInsulation)
                {
                    AdaptiveInsulation insul = (AdaptiveInsulation) entryInsul;
                        double newFactor = AdaptiveInsulation.calculateChange(insul, worldTemp, minTemp, maxTemp);
                        insul.setFactor(newFactor);
                    }
                }
            }
        }
        this.changed = true;
    }

    public void addInsulationItem(ItemStack stack)
    {
        Collection<InsulatorData> insulation = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()).stream().map(InsulatorData::copy).collect(Collectors.toList());

        if (!insulation.isEmpty())
        {   this.insulation.add(Pair.of(stack, insulation));
            this.changed = true;
        }
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, Collection<InsulatorData>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        toRemove.ifPresent(pair ->
        {
            this.insulation.remove(pair);
            this.changed = true;
        });
        return stack;
    }

    public ItemStack getInsulationItem(int index)
    {   return this.insulation.get(index).getFirst();
    }

    public boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem)
    {
        Collection<InsulatorData> insulation = ConfigSettings.INSULATION_ITEMS.get().get(insulationItem.getItem())
                                               .stream().filter(insulator -> insulator.test(null, insulationItem))
                                               .collect(Collectors.toList());
        if (insulation.isEmpty())
        {   return false;
        }

        int appliedInsulators = 0;
        for (InsulatorData data : CSMath.merge(insulation, this.getInsulators()))
        {
            // Add all slots from multi-slot insulation
            if (data.fillSlots())
            {   appliedInsulators += Insulation.splitList(data.insulation()).size();
            }
            // Single-slot insulators only count as one
            else appliedInsulators++;
        }
        appliedInsulators = Math.max(1, appliedInsulators);
        return appliedInsulators <= ItemInsulationManager.getInsulationSlots(armorItem);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        if (!this.changed && this.oldSerialized != null)
        {   return this.oldSerialized;
        }
        // Save the insulation items
        ListNBT insulNBT = new ListNBT();
        // Iterate over insulation items
        for (int i = 0; i < insulation.size(); i++)
        {
            Pair<ItemStack, Collection<InsulatorData>> entry = insulation.get(i);

            CompoundNBT entryNBT = new CompoundNBT();
            Collection<InsulatorData> insulators = entry.getSecond();
            // Store ItemStack data
            entryNBT.put("Item", entry.getFirst().save(new CompoundNBT()));
            // Store insulation data
            ListNBT entryInsulList = new ListNBT();
            for (InsulatorData insulMapping : insulators)
            {
                CompoundNBT mappingNBT = new CompoundNBT();
                mappingNBT.put("Insulator", insulMapping.serialize());
                entryInsulList.add(mappingNBT);
            }
            entryNBT.put("Values", entryInsulList);
            // Add the item to the list
            insulNBT.add(entryNBT);
        }

        CompoundNBT tag = new CompoundNBT();
        tag.put("Insulation", insulNBT);

        this.oldSerialized = tag;
        this.changed = false;
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT tag)
    {
        this.insulation.clear();

        // Load the insulation items
        ListNBT insulNBT = tag.getList("Insulation", 10);

        for (int i = 0; i < insulNBT.size(); i++)
        {
            CompoundNBT entryNBT = insulNBT.getCompound(i);
            ItemStack stack = ItemStack.of(entryNBT.getCompound("Item"));
            Collection<InsulatorData> insulators = new ArrayList<>();
            ListNBT pairListNBT = entryNBT.getList("Values", 10);
            for (int j = 0; j < pairListNBT.size(); j++)
            {
                CompoundNBT mappingNBT = pairListNBT.getCompound(j);
                InsulatorData.CODEC.decode(NBTDynamicOps.INSTANCE, mappingNBT.getCompound("Insulator")).map(Pair::getFirst).result()
                .ifPresent(insulators::add);
            }
            this.insulation.add(Pair.of(stack, insulators));
        }

        if (!tag.equals(this.oldSerialized))
        {   this.changed = true;
        }
    }

    @Override
    public void copy(IInsulatableCap cap)
    {
        this.insulation.clear();
        this.insulation.addAll(cap.getInsulation());
    }
}
