package com.momosoftworks.coldsweat.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class LootEntry
{
    public Item item;
    public Optional<CompoundNBT> tag;
    public IntegerBounds count;
    public int weight;
    public LootEntry(Item item, Optional<CompoundNBT> tag, IntegerBounds count, int weight)
    {
        this.item = item;
        this.tag = tag;
        this.count = count;
        this.weight = weight;
    }
    public static final Codec<LootEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registry.ITEM.fieldOf("item").forGetter(entry -> entry.item),
            CompoundNBT.CODEC.optionalFieldOf("nbt").forGetter(entry -> entry.tag),
            IntegerBounds.CODEC.fieldOf("count").forGetter(entry -> entry.count),
            Codec.INT.fieldOf("weight").forGetter(entry -> entry.weight)
    ).apply(instance, LootEntry::new));
}
