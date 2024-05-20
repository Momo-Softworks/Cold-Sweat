package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;
import java.util.Optional;

public record StructureTempData(List<Either<TagKey<Structure>, Structure>> structures, double temperature,
                                Temperature.Units units, boolean offset, Optional<List<String>> requiredMods)
{
    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.createVanillaTagCodec(Registries.STRUCTURE).listOf().fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.BOOL.optionalFieldOf("offset", false).forGetter(StructureTempData::offset),
            Codec.STRING.listOf().optionalFieldOf("required_mods").forGetter(StructureTempData::requiredMods)
    ).apply(instance, StructureTempData::new));

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("StructureTempData{structures=[");
        for (Either<TagKey<Structure>, Structure> structure : structures)
        {
            if (structure.left().isPresent())
            {   builder.append("#").append(structure.left().get().toString());
            }
            else
            {   builder.append(structure.right().get().toString());
            }
            builder.append(", ");
        }
        builder.append("], temperature=").append(temperature).append(", units=").append(units).append(", offset=").append(offset);
        requiredMods.ifPresent(mods -> builder.append(", requiredMods=").append(mods));
        builder.append("}");
        return builder.toString();
    }
}