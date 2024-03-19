package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationSlot;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record InsulatorData(List<Either<TagKey<Item>, Item>> items, InsulationSlot type,
                            Either<StaticInsulation, AdaptiveInsulation> insulation, Optional<CompoundTag> nbt,
                            Optional<EntityRequirement> predicate, Optional<List<String>> requiredMods)
{
    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation tagLocation = ResourceLocation.tryParse(string.replace("#", ""));
                if (tagLocation == null) throw new IllegalArgumentException("Biome tag is null");
                if (!string.contains("#")) return Either.<TagKey<Item>, Item>right(ForgeRegistries.ITEMS.getValue(tagLocation));

                return Either.<TagKey<Item>, Item>left(TagKey.create(Registry.ITEM_REGISTRY, tagLocation));
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
            InsulationSlot.CODEC.fieldOf("type").forGetter(InsulatorData::type),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).fieldOf("insulation").forGetter(InsulatorData::insulation),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(InsulatorData::nbt),
            EntityRequirement.getCodec().optionalFieldOf("predicate").forGetter(InsulatorData::predicate),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(InsulatorData::requiredMods)
    ).apply(instance, InsulatorData::new));

    public Insulation getInsulation()
    {
        if (insulation.left().isPresent())
        {   return insulation.left().get();
        }
        else if (insulation.right().isPresent())
        {   return insulation.right().get();
        }
        throw new IllegalArgumentException(String.format("Insulation %s is not defined!", insulation));
    }
}
