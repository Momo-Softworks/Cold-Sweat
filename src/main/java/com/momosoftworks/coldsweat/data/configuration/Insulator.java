package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record Insulator(Optional<Either<Item, List<Item>>> item, Optional<TagKey<Item>> tag, InsulationType type,
                        Either<StaticInsulation, AdaptiveInsulation> insulation, Optional<CompoundTag> nbt)
{
    public static final Codec<Insulator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ForgeRegistries.ITEMS.getCodec(), ForgeRegistries.ITEMS.getCodec().listOf()).optionalFieldOf("item").forGetter(Insulator::item),
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(Insulator::tag),
            InsulationType.CODEC.fieldOf("type").forGetter(Insulator::type),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).fieldOf("insulation").forGetter(Insulator::insulation),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(Insulator::nbt)
    ).apply(instance, Insulator::new));

    public Insulation getInsulation()
    {
        if (insulation.left().isPresent())
        {   return insulation.left().get();
        }
        return insulation.right().get();
    }
}
