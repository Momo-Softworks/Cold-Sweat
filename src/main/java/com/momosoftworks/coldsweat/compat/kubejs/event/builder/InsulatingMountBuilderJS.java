package com.momosoftworks.coldsweat.compat.kubejs.event.builder;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.registry.TempModifierRegistry;
import com.momosoftworks.coldsweat.data.codec.configuration.MountData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class InsulatingMountBuilderJS
{
    public NegatableList<EntityRequirement> entityPredicate = new NegatableList<>();
    public NegatableList<EntityRequirement> riderPredicate = new NegatableList<>();
    public ValueGetter<Double> coldInsulation = ValueGetter.constant(0.0);
    public ValueGetter<Double> heatInsulation = ValueGetter.constant(0.0);
    public Map<ResourceLocation, ValueGetter<Double>> modifierImmunities = new HashMap<>();

    public InsulatingMountBuilderJS()
    {}

    public InsulatingMountBuilderJS entities(String... entities)
    {
        List<EntityType<?>> entList = RegistryHelper.mapForgeRegistryTagList(ForgeRegistries.ENTITIES, ConfigHelper.getEntityTypes(entities));
        if (entList.isEmpty() && entities.length != 0)
        {   this.entityPredicate.add(EntityRequirement.NONE, true);
        }
        else
        {   this.entityPredicate.add(new EntityRequirement(entList, null), false);
        }
        return this;
    }

    public InsulatingMountBuilderJS entityPredicate(Predicate<Entity> entityPredicate)
    {   this.entityPredicate.add(new EntityRequirement(entityPredicate), false);
        return this;
    }

    public InsulatingMountBuilderJS riderPredicate(Predicate<Entity> riderPredicate)
    {   this.riderPredicate.add(new EntityRequirement(riderPredicate), false);
        return this;
    }

    public InsulatingMountBuilderJS coldInsulation(Function<Map<String, Object>, Double> function)
    {   this.coldInsulation = ValueGetter.of(function);
        return this;
    }
    public InsulatingMountBuilderJS coldInsulation(double coldInsulation)
    {   this.coldInsulation = ValueGetter.constant(coldInsulation);
        return this;
    }

    public InsulatingMountBuilderJS heatInsulation(Function<Map<String, Object>, Double> function)
    {   this.heatInsulation = ValueGetter.of(function);
        return this;
    }
    public InsulatingMountBuilderJS heatInsulation(double heatInsulation)
    {   this.heatInsulation = ValueGetter.constant(heatInsulation);
        return this;
    }

    protected InsulatingMountBuilderJS immuneToModifier(String modifierId, ValueGetter<Double> immunity)
    {
        ResourceLocation location = new ResourceLocation(modifierId);
        if (!TempModifierRegistry.getEntries().containsKey(location))
        {   ColdSweat.LOGGER.warn("Tried to add immunity to non-existent temperature modifier: {}", location);
            return this;
        }
        modifierImmunities.put(new ResourceLocation(modifierId), immunity);
        return this;
    }
    public InsulatingMountBuilderJS immuneToModifier(String modifierId, Function<Map<String, Object>, Double> immunity)
    {   return immuneToModifier(modifierId, ValueGetter.of(immunity));
    }
    public InsulatingMountBuilderJS immuneToModifier(String modifierId, double immunity)
    {   return immuneToModifier(modifierId, ValueGetter.constant(immunity));
    }

    public MountData build()
    {
        MountData data = new MountData(this.entityPredicate, this.riderPredicate, this.coldInsulation, this.heatInsulation, this.modifierImmunities);
        data.setConfigType(ConfigData.Type.KUBEJS);
        return data;
    }
}
