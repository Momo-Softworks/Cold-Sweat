package com.momosoftworks.coldsweat.data.configuration.data;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
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
    Insulation.Slot slot;
    Insulation insulation;
    ItemRequirement data;
    EntityRequirement predicate;
    Optional<AttributeModifierMap> attributes;
    Optional<List<String>> requiredMods;

    public InsulatorData(Insulation.Slot slot,
                         Insulation insulation, ItemRequirement data,
                         EntityRequirement predicate, Optional<AttributeModifierMap> attributes,
                         Optional<List<String>> requiredMods)
    {
        this.slot = slot;
        this.data = data;
        this.predicate = predicate;
        this.attributes = attributes;
        this.requiredMods = requiredMods;
    }

    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
            ItemRequirement.CODEC.fieldOf("data").forGetter(data -> data.data),
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
        tag.put("items", items);
        tag.put("tags", tags);
        tag.putString("type", slot.name());
        tag.put("insulation", insulation.serialize());
        tag.put("data", data.serialize());
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
        Insulation.Slot slot = Insulation.Slot.valueOf(nbt.getString("type"));
        Insulation insulation = Insulation.deserialize(nbt.getCompound("insulation"));
        ItemRequirement requirement = ItemRequirement.deserialize(nbt.getCompound("data"));
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

        return new InsulatorData(slot, insulation, requirement, predicate, attributes, mods);
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
