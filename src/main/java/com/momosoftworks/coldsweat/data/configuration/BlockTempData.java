package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record BlockTempData(List<Block> blocks, double temperature, double maxEffect, boolean fade, List<BlockPredicate> conditions, Optional<CompoundTag> tag) implements IForgeRegistryEntry<BlockTempData>
{
    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.BLOCKS.getCodec().listOf().fieldOf("blocks").forGetter(BlockTempData::blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(BlockTempData::temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(BlockTempData::maxEffect),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(BlockTempData::fade),
            BlockPredicate.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(BlockTempData::conditions),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(BlockTempData::tag)
    ).apply(instance, BlockTempData::new));

    @Override
    public BlockTempData setRegistryName(ResourceLocation name)
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
    public Class<BlockTempData> getRegistryType()
    {
        return BlockTempData.class;
    }
}
