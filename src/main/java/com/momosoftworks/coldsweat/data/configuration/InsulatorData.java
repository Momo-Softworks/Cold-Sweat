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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record InsulatorData(Optional<Either<Item, List<Item>>> item, Optional<TagKey<Item>> tag, InsulationType type,
                            Either<StaticInsulation, AdaptiveInsulation> insulation, Optional<CompoundTag> nbt) implements IForgeRegistryEntry<InsulatorData>
{
    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ForgeRegistries.ITEMS.getCodec(), ForgeRegistries.ITEMS.getCodec().listOf()).optionalFieldOf("item").forGetter(InsulatorData::item),
            TagKey.codec(Registry.ITEM_REGISTRY).optionalFieldOf("tag").forGetter(InsulatorData::tag),
            InsulationType.CODEC.fieldOf("type").forGetter(InsulatorData::type),
            Codec.either(StaticInsulation.CODEC, AdaptiveInsulation.CODEC).fieldOf("insulation").forGetter(InsulatorData::insulation),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(InsulatorData::nbt)
    ).apply(instance, InsulatorData::new));

    @Override
    public InsulatorData setRegistryName(ResourceLocation name)
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
    public Class<InsulatorData> getRegistryType()
    {   return InsulatorData.class;
    }

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
