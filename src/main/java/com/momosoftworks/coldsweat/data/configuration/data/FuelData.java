package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record FuelData(List<Either<TagKey<Item>, Item>> items, Double fuel, NbtRequirement nbt, Optional<List<String>> requiredMods) implements NbtSerializable
{
    public static final Codec<FuelData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation itemLocation = ResourceLocation.tryParse(string.replace("#", ""));
                if (itemLocation == null) throw new IllegalArgumentException("Biome tag is null");
                if (!string.contains("#")) return Either.<TagKey<Item>, Item>right(ForgeRegistries.ITEMS.getValue(itemLocation));

                return Either.<TagKey<Item>, Item>left(TagKey.create(Registry.ITEM_REGISTRY, itemLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Biome tag is null");
                String result = tag.left().isPresent()
                                ? "#" + tag.left().get().location()
                                : tag.right().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Biome field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("items").forGetter(FuelData::items),
            Codec.DOUBLE.fieldOf("fuel").forGetter(FuelData::fuel),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement(new CompoundTag())).forGetter(FuelData::nbt),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(FuelData::requiredMods)
    ).apply(instance, FuelData::new));

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        ListTag tags = new ListTag();
        this.items.forEach(item ->
        {   item.ifLeft(tagKey -> tags.add(StringTag.valueOf(tagKey.location().toString())));
            item.ifRight(item1 ->
            {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item1);
                if (itemId == null) return;
                items.add(StringTag.valueOf(itemId.toString()));
            });
        });
        tag.put("items", items);
        tag.putDouble("fuel", fuel);
        tag.put("tags", tags);
        tag.put("nbt", nbt.serialize());
        ListTag mods = new ListTag();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringTag.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static FuelData deserialize(CompoundTag nbt)
    {
        List<Either<TagKey<Item>, Item>> items = new ArrayList<>();
        ListTag itemsTag = nbt.getList("items", 8);
        ListTag tags = nbt.getList("tags", 8);
        for (int i = 0; i < itemsTag.size(); i++)
        {   String item = itemsTag.getString(i);
            Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
            items.add(Either.right(item1));
        }
        for (int i = 0; i < tags.size(); i++)
        {   String tag = tags.getString(i);
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tag));
            items.add(Either.left(tagKey));
        }
        Double fuel = nbt.getDouble("fuel");
        NbtRequirement nbtRequirement = NbtRequirement.deserialize(nbt.getCompound("nbt"));
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8)).map(mods ->
        {   List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < mods.size(); i++)
            {   mods1.add(mods.getString(i));
            }
            return mods1;
        });
        return new FuelData(items, fuel, nbtRequirement, requiredMods);
    }
}
