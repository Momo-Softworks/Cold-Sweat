package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.data.codec.requirement.sub_type.EntitySubRequirement;
import com.momosoftworks.coldsweat.data.codec.util.DoubleBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.scores.Team;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public record EntityRequirement(NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities,
                                LocationRequirement location, LocationRequirement steppingOn,
                                Optional<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                                EquipmentRequirement equipment, Optional<EntitySubRequirement> typeSpecificData,
                                NegatableList<String> team, Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger,
                                Optional<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature,
                                Optional<Predicate<Entity>> predicate)
{
    public EntityRequirement(NegatableList<Either<TagKey<EntityType<?>>, EntityType<?>>> entities,
                             LocationRequirement location, LocationRequirement steppingOn,
                             Optional<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                             EquipmentRequirement equipment, Optional<EntitySubRequirement> typeSpecificData,
                             NegatableList<String> team, Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger,
                             Optional<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature)
    {
        this(entities, location, steppingOn, effects, nbt, flags, equipment, typeSpecificData, team, vehicle, passenger, target, temperature, Optional.empty());;
    }

    public EntityRequirement(List<Either<TagKey<EntityType<?>>, EntityType<?>>> entities)
    {
        this(new NegatableList<>(entities), LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             Optional.empty(), new NegatableList<>(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>());
    }

    public EntityRequirement(Collection<EntityType<?>> entities, @Nullable Predicate<Entity> predicate)
    {
        this(new NegatableList<>(entities.stream().map(Either::<TagKey<EntityType<?>>, EntityType<?>>right).toList()),
             LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             Optional.empty(), new NegatableList<>(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>(),
             Optional.ofNullable(predicate));
    }

    public EntityRequirement(Predicate<Entity> predicate)
    {
        this(new NegatableList<>(), LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
            NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
            Optional.empty(), new NegatableList<>(),Optional.empty(), Optional.empty(),
             Optional.empty(), new HashMap<>(),
             Optional.ofNullable(predicate));
    }

    public static final EntityRequirement NONE = new EntityRequirement(new NegatableList<>(), LocationRequirement.NONE, LocationRequirement.NONE,
                                                                       Optional.empty(), NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
                                                                       Optional.empty(), new NegatableList<>(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>(), Optional.empty());

    public static final Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, ForgeRegistries.ENTITIES)).optionalFieldOf("entities", new NegatableList<>()).forGetter(requirement -> requirement.entities),
            LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(requirement -> requirement.location),
            LocationRequirement.CODEC.optionalFieldOf("stepping_on", LocationRequirement.NONE).forGetter(requirement -> requirement.steppingOn),
            EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
            EntitySubRequirement.CODEC.optionalFieldOf("type_specific").forGetter(requirement -> requirement.typeSpecificData),
            NegatableList.codec(Codec.STRING).optionalFieldOf("team", new NegatableList<>()).forGetter(requirement -> requirement.team),
            Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
    ).apply(instance, (type, location, standingOn, effects, nbt, flags, equipment, typeData, team, temperature) ->
            new EntityRequirement(type, location, standingOn, effects, nbt, flags, equipment, typeData, team,
                                  Optional.empty(), Optional.empty(), Optional.empty(), temperature)));

    private static final List<Codec<EntityRequirement>> REQUIREMENT_CODEC_STACK = new ArrayList<>(List.of(SIMPLE_CODEC));
    // Allow for up to 16 layers of inner codecs
    static
    {   for (int i = 0; i < 4; i++)
        {   addCodecStack();
        }
    }

    public static Codec<EntityRequirement> getCodec()
    {   return REQUIREMENT_CODEC_STACK.get(REQUIREMENT_CODEC_STACK.size() - 1);
    }

    private static void addCodecStack()
    {
        var codec = RecordCodecBuilder.<EntityRequirement>create(instance -> instance.group(
                NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, ForgeRegistries.ENTITIES)).optionalFieldOf("entities", new NegatableList<>()).forGetter(requirement -> requirement.entities),
                LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(requirement -> requirement.location),
                LocationRequirement.CODEC.optionalFieldOf("stepping_on", LocationRequirement.NONE).forGetter(requirement -> requirement.steppingOn),
                EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
                EntitySubRequirement.CODEC.optionalFieldOf("type_specific").forGetter(requirement -> requirement.typeSpecificData),
                NegatableList.codec(Codec.STRING).optionalFieldOf("team", new NegatableList<>()).forGetter(requirement -> requirement.team),
                EntityRequirement.getCodec().optionalFieldOf("vehicle").forGetter(requirement -> requirement.vehicle),
                EntityRequirement.getCodec().optionalFieldOf("passenger").forGetter(requirement -> requirement.passenger),
                EntityRequirement.getCodec().optionalFieldOf("target").forGetter(requirement -> requirement.target),
                Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
        ).apply(instance, EntityRequirement::new));

        REQUIREMENT_CODEC_STACK.add(codec);
    }

    public boolean test(Entity entity)
    {
        if (entity == null)
        {   return false;
        }
        if (this.predicate.isPresent())
        {   return this.predicate.get().test(entity);
        }
        if (Objects.equals(this, NONE))
        {   return true;
        }
        if (!entities.isEmpty())
        {
            EntityType<?> type = entity.getType();
            if (!this.entities.test(either -> either.map(type::is, type::equals)))
            {   return false;
            }
        }
        if (!location.test(entity.level, entity.position()))
        {   return false;
        }
        if (!steppingOn.test(entity.level, entity.position().add(0, -0.5, 0)))
        {   return false;
        }
        if (effects.isPresent() && !effects.get().test(entity))
        {   return false;
        }
        if (flags.isPresent() && !flags.get().test(entity))
        {   return false;
        }
        if (!equipment.test(entity))
        {   return false;
        }
        if (typeSpecificData.isPresent() && !typeSpecificData.get().test(entity, entity.level, entity.position()))
        {   return false;
        }
        if (vehicle.isPresent() && !vehicle.get().test(entity.getVehicle()))
        {   return false;
        }
        if (passenger.isPresent() && !passenger.get().test(entity.getPassengers().isEmpty() ? null : entity.getPassengers().get(0)))
        {   return false;
        }
        if (target.isPresent())
        {
            if (!(entity instanceof Monster monster) || !target.get().test(monster.getTarget()))
            {   return false;
            }
        }
        if (!team.isEmpty())
        {
            Team team = entity.getTeam();
            if (team == null || !this.team.test(t -> t.equals(team.getName())))
            {   return false;
            }
            {   return false;
            }
        }
        if (!nbt.test(entity))
        {   return false;
        }
        if (entity instanceof LivingEntity living)
        {
            if (!EntityTempManager.isTemperatureEnabled(living) && !temperature.isEmpty())
            {   return false;
            }
            for (Map.Entry<Temperature.Trait, DoubleBounds> entry : temperature.entrySet())
            {
                double value = Temperature.get(living, entry.getKey());
                if (!entry.getValue().test(value))
                {   return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString()
    {   return getCodec().encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EntityRequirement that = (EntityRequirement) obj;
        return entities.equals(that.entities)
            && location.equals(that.location)
            && steppingOn.equals(that.steppingOn)
            && effects.equals(that.effects)
            && nbt.equals(that.nbt)
            && flags.equals(that.flags)
            && equipment.equals(that.equipment)
            && typeSpecificData.equals(that.typeSpecificData)
            && vehicle.equals(that.vehicle)
            && passenger.equals(that.passenger)
            && target.equals(that.target)
            && predicate.equals(that.predicate)
            && temperature.equals(that.temperature);
    }
}