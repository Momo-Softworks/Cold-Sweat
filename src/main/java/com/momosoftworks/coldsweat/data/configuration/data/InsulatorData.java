package com.momosoftworks.coldsweat.data.configuration.data;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeCodecs;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record InsulatorData(List<Either<TagKey<Item>, Item>> items, InsulationSlot slot,
                            Insulation insulation, NbtRequirement nbt,
                            EntityRequirement predicate, Optional<AttributeModifierMap> attributes,
                            Optional<List<String>> requiredMods) implements NbtSerializable, IForgeRegistryEntry<InsulatorData>
{
    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation itemLocation = new ResourceLocation(string.replace("#", ""));
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
            .fieldOf("items").forGetter(InsulatorData::items),
            InsulationSlot.CODEC.fieldOf("type").forGetter(InsulatorData::slot),
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
            .fieldOf("insulation").forGetter(InsulatorData::insulation),
            NbtRequirement.CODEC.optionalFieldOf("nbt", new NbtRequirement(new CompoundTag())).forGetter(InsulatorData::nbt),
            EntityRequirement.getCodec().optionalFieldOf("predicate", EntityRequirement.NONE).forGetter(InsulatorData::predicate),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes").forGetter(InsulatorData::attributes),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(InsulatorData::requiredMods)
    ).apply(instance, InsulatorData::new));

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        ListTag tags = new ListTag();
        this.items.forEach(item ->
        {
            item.ifLeft(tagKey -> tags.add(StringTag.valueOf(tagKey.location().toString())));
            item.ifRight(item1 -> items.add(StringTag.valueOf(ForgeRegistries.ITEMS.getKey(item1).toString())));
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
            CompoundTag attributesTag = new CompoundTag();
            attributes1.forEach((attribute, modifier) ->
            {
                attributesTag.put(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString(),
                                  AttributeCodecs.MODIFIER_CODEC.encodeStart(NbtOps.INSTANCE, modifier).result().orElseThrow());
            });
            tag.put("attributes", attributesTag);
        }
        ListTag mods = new ListTag();
        requiredMods.ifPresent(mods1 -> mods1.forEach(mod -> mods.add(StringTag.valueOf(mod))));
        tag.put("required_mods", mods);
        return tag;
    }

    public static InsulatorData deserialize(CompoundTag nbt)
    {
        List<Either<TagKey<Item>, Item>> items = new ArrayList<>();
        ListTag itemsTag = nbt.getList("items", 8);
        ListTag tags = nbt.getList("tags", 8);
        for (int i = 0; i < itemsTag.size(); i++)
        {
            String item = itemsTag.getString(i);
            Item item1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
            items.add(Either.right(item1));
        }
        for (int i = 0; i < tags.size(); i++)
        {
            String tag = tags.getString(i);
            TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tag));
            items.add(Either.left(tagKey));
        }
        InsulationSlot type = InsulationSlot.valueOf(nbt.getString("type"));
        Insulation insulation = Insulation.deserialize(nbt.getCompound("insulation"));
        NbtRequirement nbt1 = NbtRequirement.deserialize(nbt.getCompound("nbt"));
        EntityRequirement predicate = EntityRequirement.deserialize(nbt.getCompound("predicate"));

        Optional<AttributeModifierMap> attributes = Optional.of(nbt.getCompound("attributes")).map(attributesTag ->
        {
            Map<Attribute, AttributeModifier> attributes1 = new HashMap<>();
            attributesTag.getAllKeys().forEach(key ->
            {
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
                AttributeModifier modifier = AttributeCodecs.MODIFIER_CODEC.decode(NbtOps.INSTANCE, attributesTag.get(key)).result().orElseThrow().getFirst();
                attributes1.put(attribute, modifier);
            });
            return attributes1;
        }).map(AttributeModifierMap::new);

        Optional<List<String>> mods = Optional.of(nbt.getList("required_mods", 8)).map(listTag ->
        {
            List<String> mods1 = new ArrayList<>();
            for (int i = 0; i < listTag.size(); i++)
            {
                mods1.add(listTag.getString(i));
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
