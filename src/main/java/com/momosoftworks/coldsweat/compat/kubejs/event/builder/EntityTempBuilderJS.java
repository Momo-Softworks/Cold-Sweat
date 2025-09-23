package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.configuration.EntityTempData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.function.Predicate;

public class EntityTempBuilderJS
{
    public double temperature = 0;
    public double range = 0;
    public Temperature.Units units = Temperature.Units.MC;
    public double maxEffect = 0;
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> otherEntityPredicate = new NegatableList<>();
    public boolean affectsSelf = false;

    public EntityTempBuilderJS()
    {}

    public EntityTempBuilderJS entities(String... entities)
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

    public EntityTempBuilderJS temperature(double temperature)
    {
        this.temperature = temperature;
        return this;
    }

    public EntityTempBuilderJS range(double range)
    {
        this.range = range;
        return this;
    }

    public EntityTempBuilderJS units(String units)
    {
        this.units = Temperature.Units.fromID(units);
        return this;
    }

    public EntityTempBuilderJS maxEffect(double maxEffect)
    {
        this.maxEffect = maxEffect;
        return this;
    }

    public EntityTempBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public EntityTempBuilderJS otherEntityPredicate(Predicate<Entity> otherEntityPredicate)
    {
        this.otherEntityPredicate.add(new EntityRequirement(otherEntityPredicate), false);
        return this;
    }

    public EntityTempBuilderJS affectsSelf(boolean affectsSelf)
    {
        this.affectsSelf = affectsSelf;
        return this;
    }

    public EntityTempData build()
    {
        EntityTempData data = new EntityTempData(this.entityPredicate, this.temperature, this.range, this.units, this.otherEntityPredicate, this.maxEffect, this.affectsSelf);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
