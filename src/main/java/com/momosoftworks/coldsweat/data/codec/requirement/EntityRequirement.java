package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.data.codec.requirement.sub_type.EntitySubRequirement;
import com.momosoftworks.coldsweat.data.codec.util.DoubleBounds;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.tags.ITag;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRequirement
{
    private final List<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    private final LocationRequirement location;
    private final LocationRequirement steppingOn;
    private final Optional<EffectsRequirement> effects;
    private final NbtRequirement nbt;
    private final Optional<EntityFlagsRequirement> flags;
    private final EquipmentRequirement equipment;
    private final Optional<EntitySubRequirement> typeSpecificData;
    private final List<String> team;
    private final Optional<EntityRequirement> vehicle;
    private final Optional<EntityRequirement> passenger;
    private final Optional<EntityRequirement> target;
    private final Map<Temperature.Trait, DoubleBounds> temperature;
    private final Optional<Predicate<Entity>> predicate;

    public EntityRequirement(List<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                             LocationRequirement location, LocationRequirement steppingOn,
                             Optional<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                             EquipmentRequirement equipment, Optional<EntitySubRequirement> typeSpecificData,
                             List<String> team, Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger,
                             Optional<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature,
                             Optional<Predicate<Entity>> predicate)
    {
        this.entities = entities;
        this.location = location;
        this.steppingOn = steppingOn;
        this.effects = effects;
        this.nbt = nbt;
        this.flags = flags;
        this.equipment = equipment;
        this.typeSpecificData = typeSpecificData;
        this.team = team;
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.target = target;
        this.temperature = temperature;
        this.predicate = predicate;
    }

    public EntityRequirement(List<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                             LocationRequirement location, LocationRequirement steppingOn,
                             Optional<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                             EquipmentRequirement equipment, Optional<EntitySubRequirement> typeSpecificData,
                             List<String> team, Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger,
                             Optional<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature)
    {
        this(entities, location, steppingOn, effects, nbt, flags, equipment, typeSpecificData, team, vehicle, passenger, target, temperature, Optional.empty());;
    }

    public EntityRequirement(List<Either<ITag<EntityType<?>>, EntityType<?>>> entities)
    {
        this(entities, LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             Optional.empty(), new ArrayList<>(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>());
    }

    public EntityRequirement(Collection<EntityType<?>> entities, Predicate<Entity> predicate)
    {
        this(entities.stream().map(Either::<ITag<EntityType<?>>, EntityType<?>>right).collect(Collectors.toList()),
             LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             Optional.empty(), new ArrayList<>(), Optional.empty(), Optional.empty(), Optional.empty(), new HashMap<>(),
             Optional.ofNullable(predicate));
    }

    public EntityRequirement(Predicate<Entity> predicate)
    {
        this(new ArrayList<>(), LocationRequirement.NONE, LocationRequirement.NONE, Optional.empty(),
            NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
            Optional.empty(), new ArrayList<>(),Optional.empty(), Optional.empty(),
             Optional.empty(), new HashMap<>(),
             Optional.ofNullable(predicate));
    }

    public static final EntityRequirement NONE = new EntityRequirement(new ArrayList<>(), LocationRequirement.NONE, LocationRequirement.NONE,
                                                                Optional.empty(), NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
                                                                Optional.empty(), new ArrayList<>(), Optional.empty(),
                                                                Optional.empty(), Optional.empty(), new HashMap<>(), Optional.empty());

    public static final Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, Registry.ENTITY_TYPE).listOf().optionalFieldOf("entities", Arrays.asList()).forGetter(requirement -> requirement.entities),
            LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(requirement -> requirement.location),
            LocationRequirement.CODEC.optionalFieldOf("stepping_on", LocationRequirement.NONE).forGetter(requirement -> requirement.steppingOn),
            EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
            EntitySubRequirement.CODEC.optionalFieldOf("type_specific").forGetter(requirement -> requirement.typeSpecificData),
            Codec.STRING.listOf().optionalFieldOf("team", Arrays.asList()).forGetter(requirement -> requirement.team),
            Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
    ).apply(instance, (type, location, standingOn, effects, nbt, flags, equipment, typeData, team, temperature) ->
            new EntityRequirement(type, location, standingOn, effects, nbt, flags, equipment, typeData, team,
                                  Optional.empty(), Optional.empty(), Optional.empty(), temperature)));

    private static final List<Codec<EntityRequirement>> REQUIREMENT_CODEC_STACK = new ArrayList<>(Arrays.asList(SIMPLE_CODEC));
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
        Codec<EntityRequirement> codec = RecordCodecBuilder.<EntityRequirement>create(instance -> instance.group(
                ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, Registry.ENTITY_TYPE).listOf().optionalFieldOf("entities", Arrays.asList()).forGetter(requirement -> requirement.entities),
                LocationRequirement.CODEC.optionalFieldOf("location", LocationRequirement.NONE).forGetter(requirement -> requirement.location),
                LocationRequirement.CODEC.optionalFieldOf("stepping_on", LocationRequirement.NONE).forGetter(requirement -> requirement.steppingOn),
                EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
                EntitySubRequirement.CODEC.optionalFieldOf("type_specific").forGetter(requirement -> requirement.typeSpecificData),
                Codec.STRING.listOf().optionalFieldOf("team", Arrays.asList()).forGetter(requirement -> requirement.team),
                EntityRequirement.getCodec().optionalFieldOf("vehicle").forGetter(requirement -> requirement.vehicle),
                EntityRequirement.getCodec().optionalFieldOf("passenger").forGetter(requirement -> requirement.passenger),
                EntityRequirement.getCodec().optionalFieldOf("target").forGetter(requirement -> requirement.target),
                Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
        ).apply(instance, EntityRequirement::new));

        REQUIREMENT_CODEC_STACK.add(codec);
    }

    public List<Either<ITag<EntityType<?>>, EntityType<?>>> entities()
    {   return entities;
    }
    public LocationRequirement location()
    {   return location;
    }
    public LocationRequirement steppingOn()
    {   return steppingOn;
    }
    public Optional<EffectsRequirement> effects()
    {   return effects;
    }
    public NbtRequirement nbt()
    {   return nbt;
    }
    public Optional<EntityFlagsRequirement> flags()
    {   return flags;
    }
    public EquipmentRequirement equipment()
    {   return equipment;
    }
    public Optional<EntitySubRequirement> typeSpecificData()
    {   return typeSpecificData;
    }
    public List<String> team()
    {   return team;
    }
    public Optional<EntityRequirement> vehicle()
    {   return vehicle;
    }
    public Optional<EntityRequirement> passenger()
    {   return passenger;
    }
    public Optional<EntityRequirement> target()
    {   return target;
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
        checkType:
        {
            EntityType<?> type = entity.getType();
            for (Either<ITag<EntityType<?>>, EntityType<?>> either : this.entities)
            {
                if (either.map(type::is, type::equals))
                {   break checkType;
                }
            }
            return false;
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
            if (!(entity instanceof MonsterEntity) || !target.get().test(((MonsterEntity) entity).getTarget()))
            {   return false;
            }
        }
        if (!team.isEmpty())
        {
            Team team = entity.getTeam();
            if (team == null || this.team.stream().noneMatch(str -> str.equals(team.getName())))
            {   return false;
            }
        }
        if (!nbt.test(entity))
        {   return false;
        }
        if (entity instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) entity;
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