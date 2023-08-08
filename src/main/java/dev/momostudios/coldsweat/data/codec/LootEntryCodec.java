package dev.momostudios.coldsweat.data.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public class LootEntryCodec implements Codec<LootEntryCodec.LootEntry>
{
    private final Codec<ResourceLocation> itemID;
    private final Codec<Optional<CompoundTag>> tag;
    private final Codec<Pair<Integer, Integer>> count;
    private final Codec<Integer> weight;

    public LootEntryCodec(Codec<ResourceLocation> itemID, Codec<Pair<Integer, Integer>> count, Codec<Integer> weight, Codec<Optional<CompoundTag>> tag)
    {
        this.itemID = itemID;
        this.count = count;
        this.weight = weight;
        this.tag = tag;
    }

    @Override
    public <T> DataResult<Pair<LootEntryCodec.LootEntry, T>> decode(DynamicOps<T> ops, T input)
    {
        return itemID.decode(ops, input).flatMap(itemID ->
               tag.decode(ops, input).flatMap(tag ->
               count.decode(ops, input).flatMap(count ->
               weight.decode(ops, input).map(weight ->
               Pair.of(new LootEntry(itemID.getFirst(), tag.getFirst(), count.getFirst(), weight.getFirst()), input)))));
    }

    @Override
    public <T> DataResult<T> encode(LootEntryCodec.LootEntry input, DynamicOps<T> ops, T prefix)
    {
        return itemID.decode(ops, prefix).flatMap(itemID ->
               tag.decode(ops, prefix).flatMap(tag ->
               count.decode(ops, prefix).flatMap(count ->
               weight.decode(ops, prefix).map(weight ->
               Pair.of(new LootEntry(itemID.getFirst(), tag.getFirst(), count.getFirst(), weight.getFirst()), prefix)).map(Pair::getSecond))));
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID, count, weight);
    }

    @Override
    public String toString()
    {
        return "LootEntryCodec{" +
                "item=" + itemID +
                ", count=" + count +
                ", weight=" + weight +
                '}';
    }

    public record LootEntry(ResourceLocation itemID, Optional<CompoundTag> tag, Pair<Integer, Integer> count, int weight) {}
}
