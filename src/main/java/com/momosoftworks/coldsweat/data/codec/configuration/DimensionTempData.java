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
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
import java.util.List;

public class DimensionTempData extends ConfigData
{
    final NegatableList<Either<TagKey<DimensionType>, OptionalHolder<DimensionType>>> dimensions;
    final double min;
    final double max;
    final Temperature.Units units;
    final boolean isOffset;

    public DimensionTempData(NegatableList<Either<TagKey<DimensionType>, OptionalHolder<DimensionType>>> dimensions,
                             double min, double max, Temperature.Units units, boolean isOffset,
                             NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.dimensions = dimensions;
        this.min = min;
        this.max = max;
        this.units = units;
        this.isOffset = isOffset;
    }

    public DimensionTempData(NegatableList<Either<TagKey<DimensionType>, OptionalHolder<DimensionType>>> dimensions,
                             double min, double max, Temperature.Units units, boolean isOffset)
    {
        this(dimensions, min, max, units, isOffset, new NegatableList<>());
    }

    public DimensionTempData(Holder<DimensionType> dimension, double min, double max, Temperature.Units units, boolean isOffset)
    {   this(new NegatableList<>(Either.right(OptionalHolder.ofHolder(dimension))), min, max, units, isOffset);
    }

    public static final Codec<DimensionTempData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.DIMENSION_TYPE_REGISTRY)).fieldOf("dimensions").forGetter(DimensionTempData::dimensions),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"),
                            Codec.DOUBLE.fieldOf("min_temp"))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(DimensionTempData::min),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"),
                            Codec.DOUBLE.fieldOf("max_temp"))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(DimensionTempData::max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(DimensionTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(DimensionTempData::isOffset)
    ).apply(instance, DimensionTempData::new)));

    public NegatableList<Either<TagKey<DimensionType>, OptionalHolder<DimensionType>>> dimensions()
    {   return dimensions;
    }
    public double min()
    {   return min;
    }
    public double max()
    {   return max;
    }
    public Temperature.Units units()
    {   return units;
    }
    public boolean isOffset()
    {   return isOffset;
    }

    public double getMinTemp()
    {   return Temperature.convert(min, units, Temperature.Units.MC, !isOffset);
    }
    public double getMaxTemp()
    {   return Temperature.convert(max, units, Temperature.Units.MC, !isOffset);
    }

    @Nullable
    public static DimensionTempData fromToml(List<?> entry, boolean isOffset, RegistryAccess registryAccess)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing dimension config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<DimensionType>, OptionalHolder<DimensionType>>> dimensions = ConfigHelper.parseRegistryItems(Registry.DIMENSION_TYPE_REGISTRY, registryAccess, (String) entry.get(0));
        if (dimensions.isEmpty()) return null;
        double temp = ((Number) entry.get(1)).doubleValue();
        Temperature.Units units = entry.size() == 3 ? Temperature.Units.valueOf(((String) entry.get(2)).toUpperCase()) : Temperature.Units.MC;
        return new DimensionTempData(dimensions, temp, temp, units, isOffset);
    }

    @Override
    public Codec<DimensionTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DimensionTempData that = (DimensionTempData) obj;
        return super.equals(obj)
            && Double.compare(that.min, min) == 0
            && Double.compare(that.max, max) == 0
            && isOffset == that.isOffset
            && dimensions.equals(that.dimensions)
            && units == that.units;
    }
}
