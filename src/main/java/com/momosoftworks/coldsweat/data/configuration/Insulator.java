package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Insulator(Optional<ResourceLocation> itemId, Optional<TagKey<Item>> itemTag, InsulationType type, Insulation insulation) implements IForgeRegistryEntry<Insulator>
{
    public static final Codec<Insulator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(Insulator::itemId),
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(Insulator::itemTag),
            InsulationType.CODEC.fieldOf("type").forGetter(Insulator::type),
            Insulation.CODEC.fieldOf("insulation").forGetter(Insulator::insulation)
    ).apply(instance, Insulator::new));

    @Override
    public Insulator setRegistryName(ResourceLocation name)
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
    public Class<Insulator> getRegistryType()
    {   return Insulator.class;
    }

    public record Insulation(Optional<Double> value, Optional<Double> adaptSpeed, Optional<Double> hot, Optional<Double> cold)
    {
        public static final Codec<Insulation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("value").forGetter(Insulation::value),
                Codec.DOUBLE.optionalFieldOf("adapt_speed").forGetter(Insulation::adaptSpeed),
                Codec.DOUBLE.optionalFieldOf("hot").forGetter(Insulation::hot),
                Codec.DOUBLE.optionalFieldOf("cold").forGetter(Insulation::cold)
        ).apply(instance, Insulation::new));
    }
}
