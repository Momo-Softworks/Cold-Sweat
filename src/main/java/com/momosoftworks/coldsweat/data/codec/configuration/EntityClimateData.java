package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EntityClimateData extends ConfigData implements RequirementHolder
{
    final NegatableList<EntityRequirement> entity;
    final double minTemp;
    final double maxTemp;
    final double rate;
    final Temperature.Units units;

    public EntityClimateData(NegatableList<EntityRequirement> entity, double minTemp, double maxTemp, double rate, Temperature.Units units, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.entity = entity;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.rate = rate;
        this.units = units;
    }

    public EntityClimateData(NegatableList<EntityRequirement> entity, double minTemp, double maxTemp, double rate, Temperature.Units units)
    {   this(entity, minTemp, maxTemp, rate, units, new NegatableList<>());
    }

    public static final Codec<EntityClimateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(EntityClimateData::entity),
            Codec.DOUBLE.fieldOf("min_temp_offset").forGetter(EntityClimateData::minTempOffset),
            Codec.DOUBLE.fieldOf("max_temp_offset").forGetter(EntityClimateData::maxTempOffset),
            Codec.DOUBLE.optionalFieldOf("rate", 1.0).forGetter(EntityClimateData::rate),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(EntityClimateData::units)
    ).apply(instance, EntityClimateData::new));

    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public double minTempOffset()
    {   return minTemp;
    }
    public double maxTempOffset()
    {   return maxTemp;
    }
    public double rate()
    {   return rate;
    }
    public Temperature.Units units()
    {   return units;
    }

    public double getMinTemp()
    {   return Temperature.convert(minTemp, units, Temperature.Units.MC, true);
    }
    public double getMaxTemp()
    {   return Temperature.convert(maxTemp, units, Temperature.Units.MC, true);
    }

    @Nullable
    public static EntityClimateData fromToml(List<?> entry)
    {
        if (entry.size() < 3)
        {   ColdSweat.LOGGER.error("Error parsing entity temp config: not enough arguments");
            return null;
        }
        List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities = ConfigHelper.getEntityTypes((String) entry.get(0));
        if (entities.isEmpty()) return null;

        double minTemp = ((Number) entry.get(1)).doubleValue();
        double maxTemp = ((Number) entry.get(2)).doubleValue();
        double rate = entry.size() > 3 ? ((Number) entry.get(3)).doubleValue() : 1.0;
        Temperature.Units units = entry.size() > 4 ? Temperature.Units.fromID(((String) entry.get(4)).toLowerCase(Locale.ROOT))
                                                   : Temperature.Units.MC;
        if (units == null)
        {   ColdSweat.LOGGER.error("Error parsing temperature-affected entity config {}: invalid temperature units", entry);
            return null;
        }
        return new EntityClimateData(new NegatableList<>(new EntityRequirement(entities)), minTemp, maxTemp, rate, units);
    }

    @Override
    public boolean test(Entity entity)
    {   return this.entity.test(req -> req.test(entity));
    }

    @Override
    public Codec<EntityClimateData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EntityClimateData that = (EntityClimateData) obj;
        return super.equals(obj)
            && entity.equals(that.entity)
            && Objects.equals(minTemp, that.minTemp)
            && Objects.equals(maxTemp, that.maxTemp)
            && Objects.equals(rate, that.rate)
            && units == that.units;
    }
}
