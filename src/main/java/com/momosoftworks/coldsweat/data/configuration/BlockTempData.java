package com.momosoftworks.coldsweat.data.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockTempData implements IForgeRegistryEntry<BlockTempData>
{
    List<Block> blocks;
    double temperature;
    double maxEffect;
    boolean fade;
    List<BlockState> conditions;
    Optional<CompoundNBT> tag;

    public BlockTempData(List<Block> blocks, double temperature, double maxEffect, boolean fade, List<BlockState> conditions, Optional<CompoundNBT> tag)
    {
        this.blocks = blocks;
        this.temperature = temperature;
        this.maxEffect = maxEffect;
        this.fade = fade;
        this.conditions = conditions;
        this.tag = tag;
    }

    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registry.BLOCK.listOf().fieldOf("blocks").forGetter(data -> data.blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(data -> data.maxEffect),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(data -> data.fade),
            BlockState.CODEC.listOf().optionalFieldOf("conditions", Arrays.asList()).forGetter(data -> data.conditions),
            CompoundNBT.CODEC.optionalFieldOf("tag").forGetter(data -> data.tag)
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
