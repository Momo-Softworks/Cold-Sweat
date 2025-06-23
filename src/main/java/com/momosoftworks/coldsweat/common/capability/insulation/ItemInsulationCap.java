package com.momosoftworks.coldsweat.common.capability.insulation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ItemInsulationCap implements IInsulatableCap
{
    private final List<Pair<ItemStack, List<InsulatorData>>> insulation = new ArrayList<>();
    private boolean changed = false;
    private CompoundTag serializedData = null;

    public static final Codec<Pair<ItemStack, List<InsulatorData>>> ITEM_INSULATION_PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(Pair::getFirst),
            InsulatorData.CODEC.listOf().fieldOf("insulation").forGetter(Pair::getSecond)
    ).apply(instance, Pair::new));

    public static final Codec<ItemInsulationCap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_INSULATION_PAIR_CODEC.listOf().fieldOf("insulation").forGetter(ItemInsulationCap::getInsulation)
    ).apply(instance, ItemInsulationCap::new));

    public ItemInsulationCap()
    {}

    public ItemInsulationCap(List<Pair<ItemStack, List<InsulatorData>>> insulation)
    {   this.insulation.addAll(insulation);
    }

    @Override
    public List<Pair<ItemStack, List<InsulatorData>>> getInsulation()
    {   return this.insulation;
    }

    public List<InsulatorData> getInsulators()
    {   return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).toList();
    }

    public void calcAdaptiveInsulation(double worldTemp, double minTemp, double maxTemp)
    {
        for (Pair<ItemStack, List<InsulatorData>> entry : insulation)
        {
            for (InsulatorData insulatorData : entry.getSecond())
            {
                List<Insulation> entryDataList = insulatorData.insulation();
                for (int i = 0; i < entryDataList.size(); i++)
                {
                    Insulation entryInsul = entryDataList.get(i);
                    if (entryInsul instanceof AdaptiveInsulation insul)
                    {
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
        List<InsulatorData> insulation = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()).stream().map(InsulatorData::copy).toList();

        if (!insulation.isEmpty())
        {   this.insulation.add(Pair.of(stack, insulation));
            this.changed = true;
        }
    }

    public ItemStack removeInsulationItem(ItemStack stack)
    {
        Optional<Pair<ItemStack, List<InsulatorData>>> toRemove = this.insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
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
                                               .toList();
        if (insulation.isEmpty())
        {   return false;
        }

        int appliedInsulators = 0;
        // Include builtin armor insulation as "applied" insulation
        for (InsulatorData data : ConfigSettings.INSULATING_ARMORS.get().get(armorItem.getItem()))
        {
            if (data.fillSlots())
            {   appliedInsulators += Insulation.splitList(data.insulation()).size();
            }
        }
        // Count applied insulation on the armor item
        appliedInsulators += ItemInsulationManager.getSlotsFilled(CSMath.append(insulation, this.getInsulators()));
        appliedInsulators = Math.max(1, appliedInsulators);
        return appliedInsulators <= ItemInsulationManager.getInsulationSlots(armorItem);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        if (!this.changed && this.serializedData != null)
        {   return serializedData;
        }
        DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, this);
        CompoundTag nbt = (CompoundTag) result.result().orElse(new CompoundTag());
        this.serializedData = nbt;
        this.changed = false;
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        if (Objects.equals(tag, this.serializedData)) return;
        CODEC.decode(NbtOps.INSTANCE, tag).result().ifPresent(result ->
        {
            List<Pair<ItemStack, List<InsulatorData>>> newInsulation = result.getFirst().getInsulation();
            if (!newInsulation.equals(this.insulation))
            {
                this.insulation.clear();
                this.insulation.addAll(newInsulation);
                this.changed = true;
            }
        });
    }

    @Override
    public void copy(IInsulatableCap cap)
    {
        this.insulation.clear();
        this.insulation.addAll(cap.getInsulation());
    }
}
