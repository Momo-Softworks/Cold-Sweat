package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Optional;

public class Insulator implements IForgeRegistryEntry<Insulator>
{
    public final Optional<ResourceLocation> itemId;
    public final Optional<ITag<Item>> itemTag;
    public final InsulationType type;
    public final Insulation insulation;

    public Insulator(Optional<ResourceLocation> itemId, Optional<ITag<Item>> itemTag, InsulationType type, Insulation insulation)
    {   this.itemId = itemId;
        this.itemTag = itemTag;
        this.type = type;
        this.insulation = insulation;
    }

    public static final Codec<Insulator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(insulator -> insulator.itemId),
            ITag.codec(ItemTags::getAllTags).optionalFieldOf("tag").forGetter(insulator -> insulator.itemTag),
            InsulationType.CODEC.fieldOf("type").forGetter(insulator -> insulator.type),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).xmap(either ->
            {
                return either.left().isPresent() ? either.left().get() : either.right().get();
            },
            insulation ->
            {
                if (insulation instanceof StaticInsulation)
                {   return Either.<StaticInsulation, AdaptiveInsulation>left((StaticInsulation) insulation);
                }
                if (insulation instanceof AdaptiveInsulation)
                {   return Either.<StaticInsulation, AdaptiveInsulation>right((AdaptiveInsulation) insulation);
                }
                return null;
            })
            .fieldOf("insulation").forGetter(insulator -> insulator.insulation)
    ).apply(instance, Insulator::new));

    @Override
    public Insulator setRegistryName(ResourceLocation name)
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }

    @Override
    public Class<Insulator> getRegistryType()
    {   return Insulator.class;
    }
}
