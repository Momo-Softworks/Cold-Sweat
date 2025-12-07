package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MountData extends ConfigData implements RequirementHolder
{
    final NegatableList<EntityRequirement> entity;
    final NegatableList<EntityRequirement> rider;
    final double coldInsulation;
    final double heatInsulation;
    final Map<ResourceLocation, Double> modifierImmunities;

    public MountData(NegatableList<EntityRequirement> entity, NegatableList<EntityRequirement> rider, double coldInsulation, double heatInsulation,
                     Map<ResourceLocation, Double> modifierImmunities, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.entity = entity;
        this.rider = rider;
        this.coldInsulation = coldInsulation;
        this.heatInsulation = heatInsulation;
        this.modifierImmunities = modifierImmunities;
    }

    public MountData(NegatableList<EntityRequirement> entity, NegatableList<EntityRequirement> rider, double coldInsulation, double heatInsulation,
                     Map<ResourceLocation, Double> modifierImmunities)
    {
        this(entity, rider, coldInsulation, heatInsulation, modifierImmunities, new NegatableList<>());
    }

    public static Codec<MountData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(EntityRequirement.getCodec()).fieldOf("entity").forGetter(MountData::entity),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("rider", new NegatableList<>()).forGetter(MountData::rider),
            Codec.DOUBLE.optionalFieldOf("cold_insulation", 0d).forGetter(MountData::coldInsulation),
            Codec.DOUBLE.optionalFieldOf("heat_insulation", 0d).forGetter(MountData::heatInsulation),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(MountData::modifierImmunities)
    ).apply(instance, MountData::new)));

    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public NegatableList<EntityRequirement> rider()
    {   return rider;
    }
    public double coldInsulation()
    {   return coldInsulation;
    }
    public double heatInsulation()
    {   return heatInsulation;
    }
    public Map<ResourceLocation, Double> modifierImmunities()
    {   return modifierImmunities;
    }

    @Nullable
    public static MountData fromToml(List<?> entry)
    {
        if (entry.size() < 2)
        {   ColdSweat.LOGGER.error("Error parsing insulating mount config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities = ConfigHelper.getEntityTypes((String) entry.get(0));
        if (entities.isEmpty()) return null;

        double coldInsul = ((Number) entry.get(1)).doubleValue();
        double hotInsul = entry.size() < 3
                          ? coldInsul
                          : ((Number) entry.get(2)).doubleValue();

        MountData result = new MountData(new NegatableList<>(new EntityRequirement(entities)), new NegatableList<>(), coldInsul, hotInsul, Map.of());
        result.setConfigType(Type.TOML);
        return result;
    }

    @Override
    public boolean test(Entity entity)
    {   return this.entity.test(rq -> rq.test(entity));
    }

    @Override
    public Codec<MountData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        MountData that = (MountData) obj;
        return super.equals(obj)
            && entity.equals(that.entity)
            && rider.equals(that.rider)
            && Double.compare(that.coldInsulation, coldInsulation) == 0
            && Double.compare(that.heatInsulation, heatInsulation) == 0;
    }
}
