package com.momosoftworks.coldsweat.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record LootEntry(Item item, Optional<CompoundTag> tag, IntegerBounds count, int weight)
{
    public static final Codec<LootEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(entry -> entry.item),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(entry -> entry.tag),
            IntegerBounds.CODEC.fieldOf("count").forGetter(entry -> entry.count),
            Codec.INT.fieldOf("weight").forGetter(entry -> entry.weight)
    ).apply(instance, LootEntry::new));
}
