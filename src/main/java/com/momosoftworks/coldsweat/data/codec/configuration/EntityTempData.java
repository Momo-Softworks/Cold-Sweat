package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class EntityTempData extends ConfigData implements RequirementHolder
{
    final NegatableList<EntityRequirement> entity;
    final ValueGetter<Double> temperature;
    final ValueGetter<Double> range;
    final Temperature.Units units;
    final NegatableList<EntityRequirement> affectedEntity;
    final ValueGetter<Double> maxEffect;
    final ValueGetter<Double> maxTemp;
    final ValueGetter<Double> minTemp;
    final ValueGetter<Boolean> affectsSelf;

    public EntityTempData(NegatableList<EntityRequirement> entity, ValueGetter<Double> temperature, ValueGetter<Double> range,
                          Temperature.Units units, NegatableList<EntityRequirement> affectedEntity,
                          ValueGetter<Double> maxEffect, ValueGetter<Double> maxTemp, ValueGetter<Double> minTemp,
                          ValueGetter<Boolean> affectsSelf, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.entity = entity;
        this.temperature = temperature;
        this.range = range;
        this.units = units;
        this.affectedEntity = affectedEntity;
        this.maxEffect = maxEffect;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.affectsSelf = affectsSelf;
    }

    public EntityTempData(NegatableList<EntityRequirement> entity, ValueGetter<Double> temperature, ValueGetter<Double> range,
                          Temperature.Units units, NegatableList<EntityRequirement> affectedEntity,
                          ValueGetter<Double> maxEffect, ValueGetter<Double> maxTemp, ValueGetter<Double> minTemp, ValueGetter<Boolean> affectsSelf)
    {
        this(entity, temperature, range, units, affectedEntity, maxEffect, maxTemp, minTemp, affectsSelf, new NegatableList<>());
    }

    public static final Codec<EntityTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(EntityTempData::entity),
            ValueGetter.fieldCodec("temperature", ExtraCodecs.DOUBLE, 0.0).forGetter(EntityTempData::temperature),
            ValueGetter.fieldCodec("range", ExtraCodecs.DOUBLE, 0.0).forGetter(EntityTempData::range),
            Temperature.Units.CODEC.optionalFieldOf("units", Temperature.Units.MC).forGetter(EntityTempData::units),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("affected_entity", new NegatableList<>()).forGetter(EntityTempData::affectedEntity),
            ValueGetter.optionalFieldCodec("max_effect", ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY).forGetter(EntityTempData::maxEffect),
            ValueGetter.optionalFieldCodec("max_temp", ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY).forGetter(data -> data.maxTemp),
            ValueGetter.optionalFieldCodec("min_temp", ExtraCodecs.DOUBLE, Double.NEGATIVE_INFINITY).forGetter(data -> data.minTemp),
            ValueGetter.optionalFieldCodec("affects_self", Codec.BOOL, false).forGetter(EntityTempData::affectsSelf)
    ).apply(instance, EntityTempData::new)));

    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public ValueGetter<Double> temperature()
    {   return temperature;
    }
    public ValueGetter<Double> range()
    {   return range;
    }
    public Temperature.Units units()
    {   return units;
    }
    public NegatableList<EntityRequirement> affectedEntity()
    {   return affectedEntity;
    }
    public ValueGetter<Double> maxEffect()
    {   return maxEffect;
    }
    public ValueGetter<Double> maxTemp()
    {   return maxTemp;
    }
    public ValueGetter<Double> minTemp()
    {   return minTemp;
    }
    public ValueGetter<Boolean> affectsSelf()
    {   return affectsSelf;
    }

    public double getTemperature(Entity entity, Entity affectedEntity)
    {   return Temperature.convert(temperature.get(Map.of("entity", entity, "target", affectedEntity)), this.units, Temperature.Units.MC, false);
    }
    public double getMaxEffect(Entity entity, Entity affectedEntity)
    {   return Temperature.convert(maxEffect.get(Map.of("entity", entity, "target", affectedEntity)), this.units, Temperature.Units.MC, false);
    }
    public double getMaxTemp(Entity entity, Entity affectedEntity)
    {   return Temperature.convert(maxTemp.get(Map.of("entity", entity, "target", affectedEntity)), this.units, Temperature.Units.MC, true);
    }
    public double getMinTemp(Entity entity, Entity affectedEntity)
    {   return Temperature.convert(minTemp.get(Map.of("entity", entity, "target", affectedEntity)), this.units, Temperature.Units.MC, true);
    }
    public double getRange(Entity entity, Entity affectedEntity)
    {   return range.get(Map.of("entity", entity, "target", affectedEntity));
    }
    public boolean isAffectsSelf(Entity entity, Entity affectedEntity)
    {   return affectsSelf.get(Map.of("entity", entity, "target", affectedEntity));
    }

    @Nullable
    public static EntityTempData fromToml(List<?> entry)
    {
        if (entry.size() < 3)
        {   ColdSweat.LOGGER.error("Error parsing entity temp config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities = ConfigHelper.getEntityTypes((String) entry.get(0));
        if (entities.isEmpty()) return null;

        ValueGetter<Double> temp = ValueGetter.parse(() -> entry.get(1), ExtraCodecs.DOUBLE, 0.0);
        ValueGetter<Double> range = ValueGetter.parse(() -> entry.get(2), ExtraCodecs.DOUBLE, 0.0);
        Temperature.Units units = entry.size() > 3
                                  ? Temperature.Units.fromID((String) entry.get(3))
                                  : Temperature.Units.MC;
        ValueGetter<Double> maxEffect = ValueGetter.parse(() -> entry.get(4), ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY);
        ValueGetter<Double> tempLimit = ValueGetter.parse(() -> entry.get(5), ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY);
        ValueGetter<Double> maxTemp = ValueGetter.constant(Double.POSITIVE_INFINITY);
        ValueGetter<Double> minTemp = ValueGetter.constant(Double.NEGATIVE_INFINITY);
        if (entry.size() > 5)
        {
            double tempSign = entry.get(1) instanceof Number n ? n.doubleValue() : 0.0;
            if (tempSign > 0) maxTemp = tempLimit;
            if (tempSign < 0) minTemp = tempLimit;
        }

        EntityRequirement requirement = new EntityRequirement(entities);

        EntityTempData result = new EntityTempData(new NegatableList<>(requirement), temp, range, units, new NegatableList<>(), maxEffect, maxTemp, minTemp, ValueGetter.constant(false));
        result.setConfigType(Type.TOML);
        return result;
    }

    @Override
    public boolean test(Entity entity)
    {   return this.entity.test(req -> req.test(entity));
    }

    public boolean test(Entity entity, Entity affectedEntity)
    {
        return (this.isAffectsSelf(entity, affectedEntity) || entity != affectedEntity)
            && entity.distanceTo(affectedEntity) <= getRange(entity, affectedEntity)
            && this.test(entity)
            && this.affectedEntity.test(req -> req.test(affectedEntity));
    }

    public double getTemperatureEffect(Entity entity, Entity affectedPlayer)
    {   return CSMath.blend(0, this.getTemperature(entity, affectedPlayer), entity.distanceTo(affectedPlayer), this.getRange(entity, affectedPlayer), 0);
    }

    @Override
    public Codec<EntityTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EntityTempData that = (EntityTempData) obj;
        return super.equals(obj)
            && this.temperature.equals(that.temperature)
            && this.range.equals(that.range)
            && this.entity.equals(that.entity)
            && this.units == that.units
            && this.affectedEntity.equals(that.affectedEntity)
            && this.maxEffect.equals(that.maxEffect)
            && this.maxTemp.equals(that.maxTemp)
            && this.minTemp.equals(that.minTemp)
            && this.affectsSelf == that.affectsSelf;
    }
}
