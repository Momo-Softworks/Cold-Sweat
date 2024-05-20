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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.*;

public record InsulatorData(Insulation.Slot slot,
                            Insulation insulation, ItemRequirement data,
                            EntityRequirement predicate, Optional<AttributeModifierMap> attributes,
                            Optional<List<String>> requiredMods) implements NbtSerializable, IForgeRegistryEntry<InsulatorData>
{
    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Insulation.Slot.CODEC.fieldOf("type").forGetter(InsulatorData::slot),
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
            ItemRequirement.CODEC.fieldOf("data").forGetter(InsulatorData::data),
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
        tag.put("items", items);
        tag.put("tags", tags);
        tag.putString("type", slot.name());
        tag.put("insulation", insulation.serialize());
        tag.put("data", data.serialize());
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
