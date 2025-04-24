package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;
import java.util.List;

public class StructureTempData extends ConfigData
{
    NegatableList<Either<TagKey<Structure>, Holder<Structure>>> structures;
    double temperature;
    Temperature.Units units;
    boolean isOffset;

    public StructureTempData(NegatableList<Either<TagKey<Structure>, Holder<Structure>>> structures, double temperature,
                             Temperature.Units units, boolean isOffset, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.structures = structures;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
    }

    public StructureTempData(NegatableList<Either<TagKey<Structure>, Holder<Structure>>> structures, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(structures, temperature, units, isOffset, new NegatableList<>());
    }

    public StructureTempData(Holder<Structure> structure, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(new NegatableList<>(Either.right(structure)), temperature, units, isOffset);
    }

    public static final Codec<StructureTempData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.STRUCTURE_REGISTRY, Structure.CODEC)).fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.BOOL.optionalFieldOf("offset", false).forGetter(StructureTempData::isOffset),
            NegatableList.listCodec(Codec.STRING).optionalFieldOf("required_mods", new NegatableList<>()).forGetter(ConfigData::requiredMods)
    ).apply(instance, StructureTempData::new));

    public NegatableList<Either<TagKey<Structure>, Holder<Structure>>> structures()
    {   return structures;
    }
    public double temperature()
    {   return temperature;
    }
    public Temperature.Units units()
    {   return units;
    }
    public boolean isOffset()
    {   return isOffset;
    }

    public double getTemperature()
    {   return Temperature.convert(temperature, units, Temperature.Units.MC, isOffset);
    }

    @Nullable
    public static StructureTempData fromToml(List<?> entry, boolean isOffset, RegistryAccess registryAccess)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing structure config: {} does not have enough arguments", entry);
            return null;
        }
        List<Either<TagKey<Structure>, Holder<Structure>>> structures = ConfigHelper.parseRegistryItems(Registry.STRUCTURE_REGISTRY, registryAccess, (String) entry.get(0));
        if (structures.isEmpty()) return null;
        double temp = ((Number) entry.get(1)).doubleValue();
        Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;

        return new StructureTempData(new NegatableList<>(structures), temp, units, isOffset);
    }

    @Override
    public Codec<StructureTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        StructureTempData that = (StructureTempData) obj;
        return super.equals(obj)
            && Double.compare(that.temperature, temperature) == 0
            && isOffset == that.isOffset
            && structures.equals(that.structures)
            && units == that.units;
    }
}