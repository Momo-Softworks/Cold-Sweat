package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StructureTempData extends ConfigData implements IForgeRegistryEntry<StructureTempData>
{
    NegatableList<Either<TagKey<ConfiguredStructureFeature<?, ?>>, OptionalHolder<ConfiguredStructureFeature<?, ?>>>> structures;
    double temperature;
    Temperature.Units units;
    boolean isOffset;

    public StructureTempData(NegatableList<Either<TagKey<ConfiguredStructureFeature<?, ?>>, OptionalHolder<ConfiguredStructureFeature<?, ?>>>> structures, double temperature,
                             Temperature.Units units, boolean isOffset, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.structures = structures;
        this.temperature = temperature;
        this.units = units;
        this.isOffset = isOffset;
    }

    public StructureTempData(NegatableList<Either<TagKey<ConfiguredStructureFeature<?, ?>>, OptionalHolder<ConfiguredStructureFeature<?, ?>>>> structures, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(structures, temperature, units, isOffset, new NegatableList<>());
    }

    public StructureTempData(OptionalHolder<ConfiguredStructureFeature<?, ?>> structure, double temperature,
                             Temperature.Units units, boolean isOffset)
    {
        this(new NegatableList<>(Either.right(structure)), temperature, units, isOffset);
    }

    public static final Codec<StructureTempData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY)).fieldOf("structures").forGetter(StructureTempData::structures),
            Codec.DOUBLE.fieldOf("temperature").forGetter(StructureTempData::temperature),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(StructureTempData::units),
            Codec.BOOL.optionalFieldOf("offset", false).forGetter(StructureTempData::isOffset)
    ).apply(instance, StructureTempData::new)));

    public NegatableList<Either<TagKey<ConfiguredStructureFeature<?, ?>>, OptionalHolder<ConfiguredStructureFeature<?, ?>>>> structures()
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
        NegatableList<Either<TagKey<ConfiguredStructureFeature<?, ?>>, OptionalHolder<ConfiguredStructureFeature<?, ?>>>> structures = ConfigHelper.parseRegistryItems(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, registryAccess, (String) entry.get(0));
        if (structures.isEmpty()) return null;
        double temp = ((Number) entry.get(1)).doubleValue();
        Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;

        return new StructureTempData(structures, temp, units, isOffset);
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

    @Override
    public StructureTempData setRegistryName(ResourceLocation name)
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
    public Class<StructureTempData> getRegistryType()
    {
        return null;
    }
}