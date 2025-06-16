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
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public class BiomeTempData extends ConfigData
{
    final NegatableList<Either<TagKey<Biome>, Holder<Biome>>> biomes;
    final double min;
    final double max;
    final Temperature.Units units;
    final boolean isOffset;
    final boolean isDisabled;

    public BiomeTempData(NegatableList<Either<TagKey<Biome>, Holder<Biome>>> biomes, double min, double max,
                         Temperature.Units units, boolean isOffset, boolean isDisabled, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.biomes = biomes;
        this.min = min;
        this.max = max;
        this.units = units;
        this.isOffset = isOffset;
        this.isDisabled = isDisabled;
    }

    public BiomeTempData(NegatableList<Either<TagKey<Biome>, Holder<Biome>>> biomes, double min, double max,
                         Temperature.Units units, boolean isOffset, boolean isDisabled)
    {
        this(biomes, min, max, units, isOffset, isDisabled, new NegatableList<>());
    }

    public BiomeTempData(Holder<Biome> biome, double min, double max, Temperature.Units units, boolean isOffset, boolean isDisabled)
    {   this(new NegatableList<>(Either.right(biome)), min, max, units, isOffset, isDisabled);
    }

    public static final Codec<BiomeTempData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.BIOME_REGISTRY, Biome.CODEC)).fieldOf("biomes").forGetter(BiomeTempData::biomes),
            Codec.mapEither(Codec.DOUBLE.optionalFieldOf("temperature", 0d),
                            Codec.DOUBLE.optionalFieldOf("min_temp", 0d))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(BiomeTempData::min),
            Codec.mapEither(Codec.DOUBLE.optionalFieldOf("temperature", 0d),
                            Codec.DOUBLE.optionalFieldOf("max_temp", 0d))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(BiomeTempData::max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(BiomeTempData::units),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(BiomeTempData::isOffset),
            Codec.BOOL.optionalFieldOf("disable", false).forGetter(BiomeTempData::isDisabled)
    ).apply(instance, BiomeTempData::new)));

    public NegatableList<Either<TagKey<Biome>, Holder<Biome>>> biomes()
    {   return biomes;
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
    public boolean isDisabled()
    {   return isDisabled;
    }

    public double minTemp()
    {   return Temperature.convert(min, units, Temperature.Units.MC, !this.isOffset);
    }
    public double maxTemp()
    {   return Temperature.convert(max, units, Temperature.Units.MC, !this.isOffset);
    }

    @Nullable
    public static BiomeTempData fromToml(List<?> entry, boolean isOffset, RegistryAccess registryAccess)
    {
        if (!(entry.size() == 2 && entry.get(1) instanceof String || entry.size() >= 3))
        {   ColdSweat.LOGGER.error("Error parsing biome config: not enough arguments");
            return null;
        }
        List<Either<TagKey<Biome>, Holder<Biome>>> biomes = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, registryAccess, (String) entry.get(0));
        if (biomes.isEmpty()) return null;

        Temperature.Units units;
        double min;
        double max;
        boolean isDisabled;
        // Disabled
        if (entry.get(1) instanceof String string && string.equals("disable"))
        {   units = Temperature.Units.MC;
            min = 0;
            max = 0;
            isDisabled = true;
        }
        // Normal
        else
        {   units = entry.size() == 4 ? Temperature.Units.valueOf(((String) entry.get(3)).toUpperCase()) : Temperature.Units.MC;
            min = ((Number) entry.get(1)).doubleValue();
            max = ((Number) entry.get(2)).doubleValue();
            isDisabled = false;
        }

        // Maps the biome ID to the temperature (and variance if present)
        return new BiomeTempData(new NegatableList<>(biomes), min, max, units, isOffset, isDisabled);
    }

    @Override
    public Codec<BiomeTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BiomeTempData that = (BiomeTempData) obj;
        return super.equals(obj)
            && Double.compare(that.min, min) == 0
            && Double.compare(that.max, max) == 0
            && isOffset == that.isOffset
            && biomes.equals(that.biomes)
            && units == that.units;
    }
}
