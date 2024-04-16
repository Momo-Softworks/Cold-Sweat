package com.momosoftworks.coldsweat.data.configuration.data;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeCodecs;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;

public class InsulatorData implements NbtSerializable, IForgeRegistryEntry<InsulatorData>
{
    List<Either<ITag<Item>, Item>> items;
    Insulation.Slot slot;
    Insulation insulation;
    NbtRequirement nbt;
    EntityRequirement predicate;
    Optional<AttributeModifierMap> attributes;
    Optional<List<String>> requiredMods;

    public InsulatorData(List<Either<ITag<Item>, Item>> items, Insulation.Slot slot,
                         Insulation insulation, NbtRequirement nbt,
                         EntityRequirement predicate, Optional<AttributeModifierMap> attributes,
                         Optional<List<String>> requiredMods)
    {
        this.items = items;
        this.slot = slot;
        this.nbt = nbt;
        this.predicate = predicate;
        this.attributes = attributes;
        this.requiredMods = requiredMods;
    }

    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
            {   if (tag == null) throw new IllegalArgumentException("Biome tag is null");
                String result = tag.left().isPresent()
                                ? "#" + ItemTags.getAllTags().getId(tag.left().get())
                                : tag.right().map(item -> ForgeRegistries.ITEMS.getKey(item).toString()).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Biome field is not a tag or valid ID");

                return result;
            })
            .listOf()
            .fieldOf("items").forGetter(data -> data.items),
            Insulation.Slot.CODEC.fieldOf("type").forGetter(data -> data.slot),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).xmap(either ->
            {
                return either.left().isPresent() ? either.left().get() : either.right().get();
            },
            insulation ->
            {
                if (insulation instanceof StaticInsulation)
                {   return Either.left((StaticInsulation) insulation);
                }
                if (insulation instanceof AdaptiveInsulation)
                {   return Either.right((AdaptiveInsulation) insulation);
                }
                return null;
            })
            .fieldOf("insulation").forGetter(data -> data.insulation),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement(new CompoundNBT())).forGetter(data -> data.nbt),
            EntityRequirement.getCodec().optionalFieldOf("predicate", EntityRequirement.NONE).forGetter(data -> data.predicate),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes").forGetter(data -> data.attributes),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, InsulatorData::new));

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        ListNBT items = new ListNBT();
        ListNBT tags = new ListNBT();
        this.items.forEach(item ->
        {
            item.ifLeft(tagKey -> tags.add(StringNBT.valueOf(ItemTags.getAllTags().getId(tagKey).toString())));
            item.ifRight(item1 -> items.add(StringNBT.valueOf(ForgeRegistries.ITEMS.getKey(item1).toString())));
        });
        tag.put("items", items);
        tag.put("tags", tags);
        tag.putString("type", slot.name());
        tag.put("insulation", insulation.serialize());
        tag.put("nbt", nbt.serialize());
        tag.put("predicate", predicate.serialize());
        if (attributes.isPresent())
        {
            Multimap<Attribute, AttributeModifier> attributes1 = attributes.get().getMap();
            CompoundNBT attributesTag = new CompoundNBT();
            attributes1.forEach((attribute, modifier) ->
            {
                attributesTag.put(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString(),
                                  AttributeCodecs.MODIFIER_CODEC.encodeStart(NBTDynamicOps.INSTANCE, modifier).result().orElseThrow(RuntimeException::new));
            });
            tag.put("attributes", attributesTag);
        }
        ListNBT mods = new ListNBT();
        requiredMods.ifPresent(mods1 -> mods1.forEach(mod -> mods.add(StringNBT.valueOf(mod))));
        tag.put("required_mods", mods);
        return tag;
    }

    public static InsulatorData deserialize(CompoundNBT nbt)
    {
        List<Either<ITag<Item>, Item>> items = new ArrayList<>();
        ListNBT itemsTag = nbt.getList("items", 8);
        ListNBT tags = nbt.getList("tags", 8);
        for (int i = 0; i < itemsTag.size(); i++)
        {
            String item = itemsTag.getString(i);
            Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
            items.add(Either.right(item1));
        }
        for (int i = 0; i < tags.size(); i++)
        {
            String tag = tags.getString(i);
            ITag<Item> tagKey = ItemTags.getAllTags().getTag(new ResourceLocation(tag));
            items.add(Either.left(tagKey));
        }
        Insulation.Slot type = Insulation.Slot.valueOf(nbt.getString("type"));
        Insulation insulation = Insulation.deserialize(nbt.getCompound("insulation"));
        NbtRequirement nbt1 = NbtRequirement.deserialize(nbt.getCompound("nbt"));
        EntityRequirement predicate = EntityRequirement.deserialize(nbt.getCompound("predicate"));

        Optional<AttributeModifierMap> attributes = Optional.of(nbt.getCompound("attributes")).map(attributesTag ->
        {
            Map<Attribute, AttributeModifier> attributes1 = new HashMap<>();
            attributesTag.getAllKeys().forEach(key ->
            {
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
                AttributeModifier modifier = AttributeCodecs.MODIFIER_CODEC.decode(NBTDynamicOps.INSTANCE, attributesTag.get(key)).result().orElseThrow(RuntimeException::new).getFirst();
                attributes1.put(attribute, modifier);
            });
            return attributes1;
        }).map(AttributeModifierMap::new);

        Optional<List<String>> mods = Optional.of(nbt.getList("required_mods", 8)).map(ListNBT ->
        {
            List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < ListNBT.size(); i++)
            {
                mods1.add(ListNBT.getString(i));
            }
            return mods1;
        });

        return new InsulatorData(items, type, insulation, nbt1, predicate, attributes, mods);
    }

    @Override
    public InsulatorData setRegistryName(ResourceLocation name)
    {
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation("coldsweat", "insulators");
    }

    @Override
    public Class<InsulatorData> getRegistryType()
    {
        return InsulatorData.class;
    }
}
