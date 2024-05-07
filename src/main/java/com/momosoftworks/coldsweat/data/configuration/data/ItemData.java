package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemData implements NbtSerializable, IForgeRegistryEntry<ItemData>
{
    public List<Either<ITag<Item>, Item>> items;
    public Double value;
    public NbtRequirement nbt;
    public Optional<EntityRequirement> entityRequirement;
    public Optional<List<String>> requiredMods;
    
    public ItemData(List<Either<ITag<Item>, Item>> items, Double value, NbtRequirement nbt,
                    Optional<EntityRequirement> entityRequirement, Optional<List<String>> requiredMods)
    {   this.items = items;
        this.value = value;
        this.nbt = nbt;
        this.entityRequirement = entityRequirement;
        this.requiredMods = requiredMods;
    }
    public static final Codec<ItemData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation itemLocation = new ResourceLocation(string.replace("#", ""));
                if (!string.contains("#")) return Either.<ITag<Item>, Item>right(ForgeRegistries.ITEMS.getValue(itemLocation));

                return Either.<ITag<Item>, Item>left(ItemTags.getAllTags().getTag(itemLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Item tag is null");
                String result = tag.left().isPresent()
                                ? "#" + ItemTags.getAllTags().getId(tag.left().get()).toString()
                                : tag.right().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Item field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("items").forGetter(data -> data.items),
            Codec.DOUBLE.fieldOf("value").forGetter(data -> data.value),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement(new CompoundNBT())).forGetter(data -> data.nbt),
            EntityRequirement.getCodec().optionalFieldOf("entity_requirement").forGetter(data -> data.entityRequirement),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, ItemData::new));

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT items = new ListNBT();
        ListNBT tags = new ListNBT();
        this.items.forEach(item ->
        {   item.ifLeft(tagKey -> tags.add(StringNBT.valueOf(ItemTags.getAllTags().getId(tagKey).toString())));
            item.ifRight(item1 ->
            {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item1);
                if (itemId == null) return;
                items.add(StringNBT.valueOf(itemId.toString()));
            });
        });
        tag.put("items", items);
        tag.putDouble("value", value);
        tag.put("tags", tags);
        tag.put("nbt", nbt.serialize());
        entityRequirement.ifPresent(entityRequirement1 -> tag.put("entity_requirement", entityRequirement1.serialize()));
        ListNBT mods = new ListNBT();
        requiredMods.ifPresent(mods1 ->
        {   mods1.forEach(mod -> mods.add(StringNBT.valueOf(mod)));
        });
        tag.put("required_mods", mods);
        return tag;
    }

    public static ItemData deserialize(CompoundNBT nbt)
    {
        List<Either<ITag<Item>, Item>> items = new ArrayList<>();
        ListNBT itemsTag = nbt.getList("items", 8);
        ListNBT tags = nbt.getList("tags", 8);
        for (int i = 0; i < itemsTag.size(); i++)
        {   String item = itemsTag.getString(i);
            Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
            items.add(Either.right(item1));
        }
        for (int i = 0; i < tags.size(); i++)
        {   String tag = tags.getString(i);
            ITag<Item> tagKey = ItemTags.getAllTags().getTag(new ResourceLocation(tag));
            items.add(Either.left(tagKey));
        }
        Double value = nbt.getDouble("value");
        NbtRequirement nbtRequirement = NbtRequirement.deserialize(nbt.getCompound("nbt"));
        Optional<EntityRequirement> entityRequirement = Optional.ofNullable(nbt.contains("entity_requirement")
                                                                            ? EntityRequirement.deserialize(nbt.getCompound("entity_requirement"))
                                                                            : null);
        Optional<List<String>> requiredMods = Optional.of(nbt.getList("required_mods", 8)).map(mods ->
        {   List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < mods.size(); i++)
            {   mods1.add(mods.getString(i));
            }
            return mods1;
        });
        return new ItemData(items, value, nbtRequirement, entityRequirement, requiredMods);
    }

    @Override
    public ItemData setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<ItemData> getRegistryType()
    {
        return null;
    }
}
