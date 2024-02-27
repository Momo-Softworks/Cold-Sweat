package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.util.InsulationType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class Insulator implements IForgeRegistryEntry<Insulator>
{
    Optional<Either<Item, List<Item>>> item;
    Optional<ITag<Item>> tag;
    InsulationType type;
    Either<StaticInsulation, AdaptiveInsulation> insulation;
    Optional<CompoundNBT> nbt;

    public Insulator(Optional<Either<Item, List<Item>>> itemId, Optional<ITag<Item>> itemTag, InsulationType type, Either<StaticInsulation, AdaptiveInsulation> insulation, Optional<CompoundNBT> nbt)
    {   this.item = itemId;
        this.tag = itemTag;
        this.type = type;
        this.insulation = insulation;
        this.nbt = nbt;
    }

    public static final Codec<Insulator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ForgeRegistries.ITEMS.getCodec(), ForgeRegistries.ITEMS.getCodec().listOf()).optionalFieldOf("item").forGetter(Insulator::item),
            ITag.codec(ItemTags::getAllTags).optionalFieldOf("tag").forGetter(insulator -> insulator.tag),
            InsulationType.CODEC.fieldOf("type").forGetter(insulator -> insulator.type),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).fieldOf("insulation").forGetter(insulator -> insulator.insulation),
            CompoundNBT.CODEC.optionalFieldOf("nbt").forGetter(insulator -> insulator.nbt)
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

    public Insulation getInsulation()
    {
        if (insulation.left().isPresent())
        {   return insulation.left().get();
        }
        return insulation.right().get();
    }
}
