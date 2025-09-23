package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.configuration.EntityClimateData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public class EntityClimateBuilderJS
{
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public double minTemp = -Double.MAX_VALUE;
    public double maxTemp = Double.MAX_VALUE;
    public double rate = 1;
    public Temperature.Units units = Temperature.Units.MC;

    public EntityClimateBuilderJS()
    {}

    public EntityClimateBuilderJS entities(String... entities)
    {
        Collection<EntityType<?>> entList = RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITY_TYPES, ConfigHelper.getEntityTypes(entities));
        if (entList.isEmpty() && entities.length != 0)
        {   this.entityPredicate.add(EntityRequirement.NONE, true);
        }
        else
        {   this.entityPredicate.add(new EntityRequirement(entList, null), false);
        }
        return this;
    }

    public EntityClimateBuilderJS temperature(double minTempOffset, double maxTempOffset)
    {
        this.minTemp = minTempOffset;
        this.maxTemp = maxTempOffset;
        return this;
    }

    public EntityClimateBuilderJS rate(double rate)
    {
        this.rate = rate;
        return this;
    }

    public EntityClimateBuilderJS units(String units)
    {
        this.units = Temperature.Units.fromID(units);
        return this;
    }

    public EntityClimateData build()
    {
        EntityClimateData data = new EntityClimateData(entityPredicate, minTemp, maxTemp, rate, units);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
