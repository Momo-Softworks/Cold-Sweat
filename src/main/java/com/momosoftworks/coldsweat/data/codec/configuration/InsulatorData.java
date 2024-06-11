package com.momosoftworks.coldsweat.data.codec.configuration;

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
import net.minecraft.nbt.*;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class InsulatorData implements NbtSerializable
{
    public final Insulation.Slot slot;
    public final Insulation insulation;
    public final ItemRequirement data;
    public final EntityRequirement predicate;
    public final Optional<AttributeModifierMap> attributes;
    public Map<ResourceLocation, Double> immuneTempModifiers;
    public final Optional<List<String>> requiredMods;

    public InsulatorData(Insulation.Slot slot,
                         Insulation insulation, ItemRequirement data,
                         EntityRequirement predicate, Optional<AttributeModifierMap> attributes,
                         Map<ResourceLocation, Double> immuneTempModifiers,
                         Optional<List<String>> requiredMods)
    {
        this.slot = slot;
        this.insulation = insulation;
        this.data = data;
        this.predicate = predicate;
        this.attributes = attributes;
        this.requiredMods = requiredMods;
        this.immuneTempModifiers = immuneTempModifiers;
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
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(data -> data.immuneTempModifiers),
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
        CompoundNBT immuneTempModifiersTag = new CompoundNBT();
        immuneTempModifiers.forEach((key, value) -> immuneTempModifiersTag.putDouble(key.toString(), value));
        tag.put("immune_temp_modifiers", immuneTempModifiersTag);
        if (requiredMods.isPresent())
        {   tag.put("required_mods", NBTDynamicOps.INSTANCE.createList(requiredMods.orElseGet(ArrayList::new).stream().map(StringNBT::valueOf)));
        }
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

        CompoundNBT immuneTempModifiersTag = nbt.getCompound("immune_temp_modifiers");
        Map<ResourceLocation, Double> immuneTempModifiers = new HashMap<>();
        immuneTempModifiersTag.getAllKeys().forEach(key -> immuneTempModifiers.put(new ResourceLocation(key), immuneTempModifiersTag.getDouble(key)));

        Optional<List<String>> mods = Optional.of(nbt.getList("required_mods", 8).stream().map(INBT::getAsString).collect(Collectors.toList()));

        return new InsulatorData(slot, insulation, requirement, predicate, attributes, immuneTempModifiers, mods);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("InsulatorData{slot=").append(slot).append(", insulation=").append(insulation).append(", data=").append(data).append(", predicate=").append(predicate);
        attributes.ifPresent(attributeModifierMap -> builder.append(", attributes=").append(attributeModifierMap));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append('}');
        return builder.toString();
    }
}
