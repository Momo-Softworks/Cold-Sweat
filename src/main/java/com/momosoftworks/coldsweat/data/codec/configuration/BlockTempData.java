package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.BlockRequirement;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockTempData
{
    public final List<Either<ITag<Block>, Block>> blocks;
    public final double temperature;
    public final double range;
    public final double maxEffect;
    public final boolean fade;
    public final double minTemp;
    public final double maxTemp;
    public final List<BlockRequirement> conditions;
    public final Optional<CompoundNBT> nbt;
    public final Optional<List<String>> requiredMods;

    public BlockTempData(List<Either<ITag<Block>, Block>> blocks, double temperature, double range,
                         double maxEffect, boolean fade, double maxTemp, double minTemp,
                         List<BlockRequirement> conditions, Optional<CompoundNBT> nbt, Optional<List<String>> requiredMods)
    {
        this.blocks = blocks;
        this.temperature = temperature;
        this.range = range;
        this.maxEffect = maxEffect;
        this.fade = fade;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.conditions = conditions;
        this.nbt = nbt;
        this.requiredMods = requiredMods;
    }
    public static final Codec<BlockTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.BLOCK_REGISTRY, Registry.BLOCK).listOf().fieldOf("blocks").forGetter(data -> data.blocks),
            Codec.DOUBLE.fieldOf("temperature").forGetter(data -> data.temperature),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.MAX_VALUE).forGetter(data -> data.maxEffect),
            Codec.DOUBLE.optionalFieldOf("range", Double.MAX_VALUE).forGetter(data -> data.range),
            Codec.BOOL.optionalFieldOf("fade", true).forGetter(data -> data.fade),
            Codec.DOUBLE.optionalFieldOf("max_temp", Double.MAX_VALUE).forGetter(data -> data.maxTemp),
            Codec.DOUBLE.optionalFieldOf("min_temp", -Double.MAX_VALUE).forGetter(data -> data.minTemp),
            BlockRequirement.CODEC.listOf().optionalFieldOf("conditions", Arrays.asList()).forGetter(data -> data.conditions),
            CompoundNBT.CODEC.optionalFieldOf("nbt").forGetter(data -> data.nbt),
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
        nbt.ifPresent(tag -> builder.append(", nbt=").append(tag));
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}
