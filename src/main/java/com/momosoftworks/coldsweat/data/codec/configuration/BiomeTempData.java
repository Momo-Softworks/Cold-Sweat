package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.OptionalHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BiomeTempData extends ConfigData implements IForgeRegistryEntry<BiomeTempData>
{
    final NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes;
    final double min;
    final double max;
    final Temperature.Units units;
    final double waterTemp;
    final boolean isOffset;
    final boolean isDisabled;

    public BiomeTempData(NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes, double min, double max,
                         Temperature.Units units, double waterTemp, boolean isOffset, boolean isDisabled, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.biomes = biomes;
        this.min = min;
        this.max = max;
        this.units = units;
        this.isOffset = isOffset;
        this.isDisabled = isDisabled;
        if (Double.isNaN(waterTemp)) waterTemp = isOffset ? 0 : ConfigSettings.DEFAULT_WATER_TEMPERATURE.get();
        this.waterTemp = waterTemp;
    }

    public BiomeTempData(NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes, double min, double max,
                         Temperature.Units units, double waterTemp, boolean isOffset, boolean isDisabled)
    {
        this(biomes, min, max, units, waterTemp, isOffset, isDisabled, new NegatableList<>());
    }

    public BiomeTempData(OptionalHolder<Biome> biome, double min, double max, Temperature.Units units, double waterTemp, boolean isOffset, boolean isDisabled)
    {   this(new NegatableList<>(Either.right(biome)), min, max, units, waterTemp, isOffset, isDisabled);
    }

    public static final Codec<BiomeTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrHolderCodec(Registry.BIOME_REGISTRY)).fieldOf("biomes").forGetter(BiomeTempData::biomes),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"),
                            Codec.DOUBLE.fieldOf("min_temp"))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(BiomeTempData::min),
            Codec.mapEither(Codec.DOUBLE.fieldOf("temperature"),
                            Codec.DOUBLE.fieldOf("max_temp"))
                 .xmap(either -> either.map(left -> left, right -> right), Either::right)
                 .forGetter(BiomeTempData::max),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(BiomeTempData::units),
            Codec.DOUBLE.optionalFieldOf("water_temp", Double.NaN).forGetter(BiomeTempData::waterTemp),
            Codec.BOOL.optionalFieldOf("is_offset", false).forGetter(BiomeTempData::isOffset),
            Codec.BOOL.optionalFieldOf("disable", false).forGetter(BiomeTempData::isDisabled)
    ).apply(instance, BiomeTempData::new)));

    public NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes()
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
    public double waterTemp()
    {   return waterTemp;
    }
    public boolean isOffset()
    {   return isOffset;
    }
    public boolean isDisabled()
    {   return isDisabled;
    }

    public double getMinTemp()
    {   return Temperature.convert(min, units, Temperature.Units.MC, !this.isOffset);
    }
    public double getMaxTemp()
    {   return Temperature.convert(max, units, Temperature.Units.MC, !this.isOffset);
    }
    public double getWaterTemp()
    {   return Temperature.convert(waterTemp, units, Temperature.Units.MC, false);
    }

    @Nullable
    public static BiomeTempData fromToml(List<?> entry, boolean isOffset, RegistryAccess registryAccess)
    {
        if (!(entry.size() == 2 && entry.get(1) instanceof String || entry.size() >= 3))
        {   ColdSweat.LOGGER.error("Error parsing biome config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<Biome>, OptionalHolder<Biome>>> biomes = ConfigHelper.parseRegistryItems(Registry.BIOME_REGISTRY, registryAccess, (String) entry.get(0));
        if (biomes.isEmpty()) return null;

        Temperature.Units units = Temperature.Units.MC;
        double min = 0;
        double max = 0;
        double waterTemp = Double.NaN;
        boolean isDisabled = false;
        // Disabled
        if (entry.get(1) instanceof String string && string.equals("disable"))
        {   isDisabled = true;
        }
        // Normal
        else
        {
            min = ((Number) entry.get(1)).doubleValue();
            max = ((Number) entry.get(2)).doubleValue();
            if (entry.size() >= 4) units = Temperature.Units.fromID(((String) entry.get(3)).toUpperCase());
            if (entry.size() >= 5) waterTemp = ((Number) entry.get(4)).doubleValue();
        }
        BiomeTempData result = new BiomeTempData(biomes, min, max, units, waterTemp, isOffset, isDisabled);
        result.setConfigType(Type.TOML);
        return result;
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

    @Override
    public BiomeTempData setRegistryName(ResourceLocation name)
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
    public Class<BiomeTempData> getRegistryType()
    {
        return null;
    }
}
