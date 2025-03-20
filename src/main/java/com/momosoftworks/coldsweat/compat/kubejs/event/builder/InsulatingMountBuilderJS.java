package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.data.codec.configuration.MountData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.function.Predicate;

public class InsulatingMountBuilderJS
{
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> riderPredicate = new NegatableList<>();
    public double coldInsulation = 0;
    public double heatInsulation = 0;

    public InsulatingMountBuilderJS()
    {}

    public InsulatingMountBuilderJS entities(String... entities)
    {
        List<EntityType<?>> entityList = RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ENTITY_TYPE, ConfigHelper.getEntityTypes(entities));
        this.entityPredicate.add(new EntityRequirement(entityList, null), false);
        return this;
    }

    public InsulatingMountBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {
        this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public InsulatingMountBuilderJS riderPredicate(Predicate<Entity> riderPredicate)
    {
        this.riderPredicate.add(new EntityRequirement(riderPredicate), false);
        return this;
    }

    public InsulatingMountBuilderJS coldInsulation(double coldInsulation)
    {
        this.coldInsulation = coldInsulation;
        return this;
    }

    public InsulatingMountBuilderJS heatInsulation(double heatInsulation)
    {
        this.heatInsulation = heatInsulation;
        return this;
    }

    public MountData build()
    {
        MountData data = new MountData(this.entityPredicate, this.riderPredicate, this.coldInsulation, this.heatInsulation);
        data.setType(ConfigData.Type.KUBEJS);
        return data;
    }
}
