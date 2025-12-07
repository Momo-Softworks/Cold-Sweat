package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;

import javax.annotation.Nullable;
import java.util.List;

public class StructureTempData extends ConfigData
{
    NegatableList<StructureFeature<?, ?>> structures;
    double temperature;
    Temperature.Units units;
    boolean isOffset;

    public StructureTempData(NegatableList<StructureFeature<?, ?>> structures, double temperature,
                             Temperature.Units units, boolean isOffset, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.structures = structures;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
    }

    public StructureTempData(NegatableList<StructureFeature<?, ?>> structures, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(structures, temperature, units, isOffset, new NegatableList<>());
    }

    public StructureTempData(StructureFeature<?, ?> structure, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(new NegatableList<>(structure), temperature, units, isOffset);
    }

    public static final Codec<StructureTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.dynamicCodec(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY)).fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.BOOL.optionalFieldOf("offset", false).forGetter(StructureTempData::isOffset)
    ).apply(instance, StructureTempData::new)));

    public NegatableList<StructureFeature<?, ?>> structures()
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
    public static StructureTempData fromToml(List<?> entry, boolean isOffset, DynamicRegistries registryAccess)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing structure config: {} does not have enough arguments", entry);
            return null;
        }
        NegatableList<StructureFeature<?, ?>> structures = ConfigHelper.parseRegistryItems(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, registryAccess, (String) entry.get(0));
        if (structures.isEmpty()) return null;
        double temp = ((Number) entry.get(1)).doubleValue();
        Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;

        StructureTempData result = new StructureTempData(structures, temp, units, isOffset);
        result.setConfigType(Type.TOML);
        return result;
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