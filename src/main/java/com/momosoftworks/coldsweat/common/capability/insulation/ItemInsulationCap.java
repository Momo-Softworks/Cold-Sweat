package com.momosoftworks.coldsweat.common.capability.insulation;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.util.StreamCodecs;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public record ItemInsulationCap(List<Pair<ItemStack, List<InsulatorData>>> insulation)
{
    public static final Codec<Pair<ItemStack, List<InsulatorData>>> ITEM_INSULATION_PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(Pair::getFirst),
            InsulatorData.CODEC.listOf().fieldOf("insulation").forGetter(Pair::getSecond)
    ).apply(instance, Pair::new));

    public static final Codec<ItemInsulationCap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ITEM_INSULATION_PAIR_CODEC.listOf().fieldOf("insulation").forGetter(ItemInsulationCap::insulation)
    ).apply(instance, ItemInsulationCap::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemInsulationCap> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public ItemInsulationCap()
    {   this(new ArrayList<>());
    }

    public List<Pair<ItemStack, List<InsulatorData>>> getInsulation()
    {   return ImmutableList.copyOf(this.insulation());
    }

    public List<InsulatorData> getInsulators()
    {   return this.insulation.stream().map(Pair::getSecond).flatMap(Collection::stream).toList();
    }

    public ItemInsulationCap calcAdaptiveInsulation(double worldTemp, double minTemp, double maxTemp)
    {
        var insulation = new ArrayList<>(this.insulation());
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
        return new ItemInsulationCap(insulation);
    }

    public ItemInsulationCap addInsulationItem(ItemStack stack)
    {
        var insulation = new ArrayList<>(this.insulation());

        List<InsulatorData> newInsulation = ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()).stream().map(InsulatorData::copy).toList();

        if (!newInsulation.isEmpty())
        {   insulation.add(Pair.of(stack, newInsulation));
        }
        return new ItemInsulationCap(insulation);
    }

    public ItemInsulationCap removeInsulationItem(ItemStack stack)
    {
        var insulation = new ArrayList<>(this.insulation());
        Optional<Pair<ItemStack, List<InsulatorData>>> toRemove = insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        toRemove.ifPresent(insulation::remove);

        return new ItemInsulationCap(insulation);
    }

    public ItemStack getInsulationItem(int index)
    {   return this.insulation().get(index).getFirst();
    }

    public boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem)
    {
        List<InsulatorData> insulation = ConfigSettings.INSULATION_ITEMS.get().get(insulationItem.getItem())
                                               .stream().filter(insulator -> insulator.test(null, insulationItem))
                                               .toList();
        if (insulation.isEmpty())
        {   return false;
        }

        int appliedInsulators = 0;
        for (InsulatorData data : CSMath.append(insulation, this.getInsulators()))
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

    public void serialize(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(this.insulation().size());
        // Iterate over insulation items
        for (int i = 0; i < this.insulation().size(); i++)
        {
            Pair<ItemStack, List<InsulatorData>> entry = this.insulation().get(i);
            // Store ItemStack data
            ItemStack.STREAM_CODEC.encode(buffer, entry.getFirst());
            // Store insulation data
            Collection<InsulatorData> insulList = entry.getSecond();
            buffer.writeCollection(insulList, (StreamEncoder) InsulatorData.SIMPLE_STREAM_CODEC);
        }
    }

    public static ItemInsulationCap deserialize(RegistryFriendlyByteBuf buffer)
    {
        List<Pair<ItemStack, List<InsulatorData>>> insulation = new ArrayList<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            // Read ItemStack data
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            // Read insulation data
            List<InsulatorData> insulList = buffer.readList((StreamDecoder) InsulatorData.SIMPLE_STREAM_CODEC);
            insulation.add(Pair.of(stack, insulList));
        }
        return new ItemInsulationCap(insulation);
    }
}
