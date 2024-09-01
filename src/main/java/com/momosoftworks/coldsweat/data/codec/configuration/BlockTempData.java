package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BlockTempData
{
    public final List<Either<ITag<Block>, Block>> blocks;
    public final double temperature;
    public final double range;
    public final double maxEffect;
    public final boolean fade;
    public final List<BlockState> conditions;
    public final Optional<CompoundNBT> tag;
    public final Optional<List<String>> requiredMods;

    private ResourceLocation registryName = new ResourceLocation(ColdSweat.MOD_ID, UUID.randomUUID().toString());

    public BlockTempData(List<Either<ITag<Block>, Block>> blocks, double temperature, double range, double maxEffect, boolean fade,
                         List<BlockState> conditions, Optional<CompoundNBT> tag, Optional<List<String>> requiredMods)
    {
        this.blocks = blocks;
        this.temperature = temperature;
        this.range = range;
        this.maxEffect = maxEffect;
        this.fade = fade;
        this.conditions = conditions;
        this.tag = tag;
        this.requiredMods = requiredMods;
    }

    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrRegistryObjectCodec(Registry.BLOCK_REGISTRY, Registry.BLOCK).listOf().fieldOf("blocks").forGetter(data -> data.blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(data -> data.maxEffect),
            Codec.DOUBLE.optionalFieldOf("range", Double.MAX_VALUE).forGetter(data -> data.range),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(data -> data.fade),
            BlockState.CODEC.listOf().optionalFieldOf("conditions", Arrays.asList()).forGetter(data -> data.conditions),
            CompoundNBT.CODEC.optionalFieldOf("nbt").forGetter(data -> data.tag),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(data -> data.requiredMods)
    ).apply(instance, BlockTempData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BlockTempData{blocks=[");
        for (Either<ITag<Block>, Block> block : blocks)
        {
            if (block.left().isPresent())
            {   builder.append("#").append(block.left().get().toString());
            }
            else
            {   builder.append(block.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", range=").append(range).append(", maxEffect=").append(maxEffect).append(", fade=").append(fade).append(", condition=").append(conditions);
        tag.ifPresent(tag -> builder.append(", nbt=").append(tag));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
