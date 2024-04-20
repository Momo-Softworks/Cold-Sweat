package com.momosoftworks.coldsweat.data.configuration.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

public record BlockTempData(List<Either<TagKey<Block>, Block>> blocks, double temperature, double range, double maxEffect, boolean fade,
                            BlockPredicate condition, Optional<CompoundTag> nbt, Optional<List<String>> requiredMods)
{
    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(
            // Convert from a string to a TagKey
            string ->
            {
                ResourceLocation tagLocation = new ResourceLocation(string.replace("#", ""));
                if (!string.contains("#")) return Either.<TagKey<Block>, Block>right(ForgeRegistries.BLOCKS.getValue(tagLocation));

                return Either.<TagKey<Block>, Block>left(TagKey.create(Registry.BLOCK_REGISTRY, tagLocation));
            },
            // Convert from a TagKey to a string
            tag ->
            {   if (tag == null) throw new IllegalArgumentException("Biome tag is null");
                String result = tag.left().isPresent()
                                ? "#" + tag.left().get().location()
                                : tag.right().map(block -> ForgeRegistries.BLOCKS.getKey(block).toString()).orElse("");
                if (result.isEmpty()) throw new IllegalArgumentException("Biome field is not a tag or valid ID");
                return result;
            })
            .listOf()
            .fieldOf("blocks").forGetter(BlockTempData::blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(BlockTempData::temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(BlockTempData::maxEffect),
            Codec.DOUBLE.optionalFieldOf("range", Double.MAX_VALUE).forGetter(BlockTempData::range),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(BlockTempData::fade),
            BlockPredicate.CODEC.optionalFieldOf("condition", BlockPredicate.alwaysTrue()).forGetter(BlockTempData::condition),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(BlockTempData::nbt),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(BlockTempData::requiredMods)
    ).apply(instance, BlockTempData::new));
}
