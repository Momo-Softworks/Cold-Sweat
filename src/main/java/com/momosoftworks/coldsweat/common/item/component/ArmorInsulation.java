package com.momosoftworks.coldsweat.common.item.component;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public record ArmorInsulation(List<Pair<ItemStack, List<Insulation>>> insulation)
{
    public static final Codec<List<Insulation>> INSULATION_CODEC = CompoundTag.CODEC.xmap(
    tag ->
    {
        List<Insulation> insulList = new ArrayList<>();
        for (Tag insulTag : tag.getList("Insulation", 10))
        {   insulList.add(Insulation.deserialize(((CompoundTag) insulTag)));
        }
        return insulList;
    },
    list ->
    {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Insulation value : list)
        {   listTag.add(value.serialize());
        }
        tag.put("Insulation", listTag);
        return tag;
    });

    public static final Codec<ArmorInsulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.pair(ItemStack.CODEC, INSULATION_CODEC).listOf().fieldOf("Insulation").forGetter(armorInsulation -> armorInsulation.insulation)
    ).apply(instance, ArmorInsulation::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorInsulation> STREAM_CODEC = StreamCodec.of((buf, insul) -> insul.serialize(buf),
                                                                                                            (buf) -> ArmorInsulation.deserialize(buf));

    public ArmorInsulation()
    {   this(new ArrayList<>());
    }

    public List<Pair<ItemStack, List<Insulation>>> getInsulation()
    {   return ImmutableList.copyOf(this.insulation());
    }

    public ArmorInsulation calcAdaptiveInsulation(double worldTemp, double minTemp, double maxTemp)
    {
        var insulation = new ArrayList<>(this.insulation());
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
        return new ArmorInsulation(insulation);
    }

    public ArmorInsulation addInsulationItem(ItemStack stack)
    {
        var insulation = new ArrayList<>(this.insulation());
        ConfigSettings.INSULATION_ITEMS.get().computeIfPresent(stack.getItem(), (item, insulator) ->
        {
            insulation.add(Pair.of(stack, insulator.insulation().split()));
            return insulator;
        });
        return new ArmorInsulation(insulation);
    }

    public ArmorInsulation removeInsulationItem(ItemStack stack)
    {
        var insulation = new ArrayList<>(this.insulation());
        Optional<Pair<ItemStack, List<Insulation>>> toRemove = insulation.stream().filter(entry -> entry.getFirst().equals(stack)).findFirst();
        toRemove.ifPresent(insulation::remove);

        return new ArmorInsulation(insulation);
    }

    public ItemStack getInsulationItem(int index)
    {   return this.insulation().get(index).getFirst();
    }

    public boolean canAddInsulationItem(ItemStack armorItem, ItemStack insulationItem)
    {
        AtomicInteger positiveInsul = new AtomicInteger();

        Insulator insulator = ConfigSettings.INSULATION_ITEMS.get().get(insulationItem.getItem());
        if (insulator == null)
        {   return false;
        }

        List<Pair<ItemStack, List<Insulation>>> insulList = new ArrayList<>(this.insulation);
        insulList.add(Pair.of(insulationItem, insulator.insulation().split()));

        // Get the total positive/negative insulation of the armor
        insulList.stream().map(Pair::getSecond).flatMap(Collection::stream).forEach(insul ->
        {
            if (insul.getHeat() >= 0 || insul.getCold() >= 0)
            {   positiveInsul.getAndIncrement();
            }
        });
        return positiveInsul.get() <= ItemInsulationManager.getInsulationSlots(armorItem);
    }

    public void serialize(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(this.insulation().size());
        // Iterate over insulation items
        for (int i = 0; i < this.insulation().size(); i++)
        {
            Pair<ItemStack, List<Insulation>> entry = this.insulation().get(i);
            List<Insulation> insulList = entry.getSecond();
            // Store ItemStack data
            ItemStack.STREAM_CODEC.encode(buffer, entry.getFirst());
            // Store insulation data
            buffer.writeObjectCollection(insulList, (insul, buf) -> Insulation.getNetworkCodec().encode(buf, insul));
        }
    }

    public static ArmorInsulation deserialize(RegistryFriendlyByteBuf buffer)
    {
        int size = buffer.readInt();
        List<Pair<ItemStack, List<Insulation>>> insulation = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            List<Insulation> insulList = buffer.readList(buf -> Insulation.getNetworkCodec().decode(buf));
            insulation.add(Pair.of(stack, insulList));
        }
        return new ArmorInsulation(insulation);
    }
}
