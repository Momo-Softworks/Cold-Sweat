package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.configuration.EntityTempData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.WorldTempRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntityTempBuilderJS
{
    public ValueGetter<Double> temperature = ValueGetter.constant(0.0);
    public ValueGetter<Double> range = ValueGetter.constant(0.0);
    public Temperature.Units units = Temperature.Units.MC;
    public ValueGetter<Double> maxEffect = ValueGetter.constant(Double.POSITIVE_INFINITY);
    public ValueGetter<Double> maxTemperature = ValueGetter.constant(Double.POSITIVE_INFINITY);
    public ValueGetter<Double> minTemperature = ValueGetter.constant(Double.NEGATIVE_INFINITY);
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> otherEntityPredicate = new NegatableList<>();
    public ValueGetter<Boolean> affectsSelf = ValueGetter.constant(false);

    public EntityTempBuilderJS()
    {}

    public EntityTempBuilderJS entities(String... entities)
    {
        Collection<EntityType<?>> entList = RegistryHelper.mapTaggableList(ConfigHelper.getEntityTypes(entities));
        if (entList.isEmpty() && entities.length != 0)
        {   this.entityPredicate.add(EntityRequirement.NONE, true);
        }
        else
        {   this.entityPredicate.add(new EntityRequirement(entList, null), false);
        }
        return this;
    }

    public EntityTempBuilderJS temperature(Function<Map<String, Object>, Double> function)
    {
        this.temperature = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS temperature(double temperature)
    {   this.temperature = ValueGetter.constant(temperature);
        return this;
    }

    public EntityTempBuilderJS range(Function<Map<String, Object>, Double> function)
    {   this.range = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS range(double range)
    {   this.range = ValueGetter.constant(range);
        return this;
    }

    public EntityTempBuilderJS units(String units)
    {   this.units = Temperature.Units.fromID(units);
        return this;
    }

    public EntityTempBuilderJS maxEffect(Function<Map<String, Object>, Double> function)
    {   this.maxEffect = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS maxEffect(double maxEffect)
    {   this.maxEffect = ValueGetter.constant(maxEffect);
        return this;
    }

    public EntityTempBuilderJS maxTemperature(Function<Map<String, Object>, Double> function)
    {   this.maxTemperature = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS maxTemperature(double maxTemperature)
    {   double converted = Temperature.convert(maxTemperature, units, Temperature.Units.MC, true);
        this.maxTemperature = ValueGetter.constant(converted);
        return this;
    }

    public EntityTempBuilderJS minTemperature(Function<Map<String, Object>, Double> function)
    {   this.minTemperature = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS minTemperature(double minTemperature)
    {   double converted = Temperature.convert(minTemperature, units, Temperature.Units.MC, true);
        this.minTemperature = ValueGetter.constant(converted);
        return this;
    }

    public EntityTempBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {   this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public EntityTempBuilderJS otherEntityPredicate(Predicate<Entity> otherEntityPredicate)
    {   this.otherEntityPredicate.add(new EntityRequirement(otherEntityPredicate), false);
        return this;
    }

    public EntityTempBuilderJS affectsSelf(Function<Map<String, Object>, Boolean> function)
    {   this.affectsSelf = ValueGetter.of(function);
        return this;
    }
    public EntityTempBuilderJS affectsSelf(boolean affectsSelf)
    {   this.affectsSelf = ValueGetter.constant(affectsSelf);
        return this;
    }

    public EntityTempData build()
    {
        EntityTempData data = new EntityTempData(this.entityPredicate, this.temperature, this.range, this.units, this.otherEntityPredicate, this.maxEffect,
                                                 this.maxTemperature, this.minTemperature, this.affectsSelf);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
