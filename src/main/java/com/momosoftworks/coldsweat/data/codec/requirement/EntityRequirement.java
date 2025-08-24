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
    private final NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> entities;
    private final NegatableList<LocationRequirement> location;
    private final NegatableList<EffectsRequirement> effects;
    private final NbtRequirement nbt;
    private final Optional<EntityFlagsRequirement> flags;
    private final EquipmentRequirement equipment;
    private final NegatableList<EntitySubRequirement> typeSpecificData;
    private final NegatableList<String> team;
    private final NegatableList<EntityRequirement> vehicle;
    private final NegatableList<EntityRequirement> passenger;
    private final NegatableList<EntityRequirement> target;
    private final Map<Temperature.Trait, DoubleBounds> temperature;
    private final Optional<Predicate<Entity>> predicate;

    public EntityRequirement(NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                             NegatableList<LocationRequirement> location,
                             NegatableList<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                             EquipmentRequirement equipment, NegatableList<EntitySubRequirement> typeSpecificData,
                             NegatableList<String> team, NegatableList<EntityRequirement> vehicle, NegatableList<EntityRequirement> passenger,
                             NegatableList<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature,
                             Optional<Predicate<Entity>> predicate)
    {
        this.entities = entities;
        this.location = location;
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

    public EntityRequirement(NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> entities,
                             NegatableList<LocationRequirement> location,
                             NegatableList<EffectsRequirement> effects, NbtRequirement nbt, Optional<EntityFlagsRequirement> flags,
                             EquipmentRequirement equipment, NegatableList<EntitySubRequirement> typeSpecificData,
                             NegatableList<String> team, NegatableList<EntityRequirement> vehicle, NegatableList<EntityRequirement> passenger,
                             NegatableList<EntityRequirement> target, Map<Temperature.Trait, DoubleBounds> temperature)
    {
        this(entities, location, effects, nbt, flags, equipment, typeSpecificData, team, vehicle, passenger, target, temperature, Optional.empty());;
    }

    public EntityRequirement(NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> entities)
    {
        this(entities, new NegatableList<>(), new NegatableList<>(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new HashMap<>());
    }

    public EntityRequirement(Collection<EntityType<?>> entities, Predicate<Entity> predicate)
    {
        this(new NegatableList<>(entities.stream().map(Either::<ITag<EntityType<?>>, EntityType<?>>right).collect(Collectors.toList())),
             new NegatableList(LocationRequirement.NONE), new NegatableList<>(),
             NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
             new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new HashMap<>(),
             Optional.ofNullable(predicate));
    }

    public EntityRequirement(Predicate<Entity> predicate)
    {   this(new ArrayList<>(), predicate);
    }

    public static final EntityRequirement NONE = new EntityRequirement(new NegatableList<>(), new NegatableList<>(),
                                                                new NegatableList<>(), NbtRequirement.NONE, Optional.empty(), EquipmentRequirement.NONE,
                                                                 new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), new HashMap<>(), Optional.empty());

    public static final Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, Registry.ENTITY_TYPE)).optionalFieldOf("entities", new NegatableList<>()).forGetter(requirement -> requirement.entities),
            NegatableList.codec(LocationRequirement.CODEC).optionalFieldOf("location", new NegatableList<>()).forGetter(requirement -> requirement.location),
            NegatableList.codec(EffectsRequirement.CODEC).optionalFieldOf("effects", new NegatableList<>()).forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
            NegatableList.codec(EntitySubRequirement.CODEC).optionalFieldOf("type_specific", new NegatableList<>()).forGetter(requirement -> requirement.typeSpecificData),
            NegatableList.codec(Codec.STRING).optionalFieldOf("team", new NegatableList<>()).forGetter(requirement -> requirement.team),
            Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
    ).apply(instance, (type, location, effects, nbt, flags, equipment, typeData, team, temperature) ->
            new EntityRequirement(type, location, effects, nbt, flags, equipment, typeData, team,
                                  new NegatableList<>(), new NegatableList<>(), new NegatableList<>(), temperature)));

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
                NegatableList.listCodec(ConfigHelper.tagOrBuiltinCodec(Registry.ENTITY_TYPE_REGISTRY, Registry.ENTITY_TYPE)).optionalFieldOf("entities", new NegatableList<>()).forGetter(requirement -> requirement.entities),
                NegatableList.codec(LocationRequirement.CODEC).optionalFieldOf("location", new NegatableList<>()).forGetter(requirement -> requirement.location),
                NegatableList.codec(EffectsRequirement.CODEC).optionalFieldOf("effects", new NegatableList<>()).forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt", NbtRequirement.NONE).forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment", EquipmentRequirement.NONE).forGetter(requirement -> requirement.equipment),
                NegatableList.codec(EntitySubRequirement.CODEC).optionalFieldOf("type_specific", new NegatableList<>()).forGetter(requirement -> requirement.typeSpecificData),
                NegatableList.codec(Codec.STRING).optionalFieldOf("team", new NegatableList<>()).forGetter(requirement -> requirement.team),
                NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("vehicle", new NegatableList<>()).forGetter(requirement -> requirement.vehicle),
                NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("passenger", new NegatableList<>()).forGetter(requirement -> requirement.passenger),
                NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("target", new NegatableList<>()).forGetter(requirement -> requirement.target),
                Codec.unboundedMap(Temperature.Trait.CODEC, DoubleBounds.CODEC).optionalFieldOf("temperature", new HashMap<>()).forGetter(requirement -> requirement.temperature)
        ).apply(instance, EntityRequirement::new));

        REQUIREMENT_CODEC_STACK.add(codec);
    }

    public NegatableList<Either<ITag<EntityType<?>>, EntityType<?>>> entities()
    {   return entities;
    }
    public NegatableList<LocationRequirement> location()
    {   return location;
    }
    public NegatableList<EffectsRequirement> effects()
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
    public NegatableList<EntitySubRequirement> typeSpecificData()
    {   return typeSpecificData;
    }
    public NegatableList<String> team()
    {   return team;
    }
    public NegatableList<EntityRequirement> vehicle()
    {   return vehicle;
    }
    public NegatableList<EntityRequirement> passenger()
    {   return passenger;
    }
    public NegatableList<EntityRequirement> target()
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
        {
            EntityType<?> type = entity.getType();
            if (!this.entities.test(either -> either.map(type::is, type::equals)))
            {   return false;
            }
        }
        if (!location.test(req -> req.test(entity.level, entity.position())))
        {   return false;
        }
        if (effects.test(req -> req.test(entity)))
        {   return false;
        }
        if (flags.isPresent() && !flags.get().test(entity))
        {   return false;
        }
        if (!equipment.test(entity))
        {   return false;
        }
        if (typeSpecificData.test(req -> req.test(entity, entity.level, entity.position())))
        {   return false;
        }
        if (vehicle.test(req -> req.test(entity.getVehicle())))
        {   return false;
        }
        if (passenger.test(req -> req.test(entity.getPassengers().isEmpty() ? null : entity.getPassengers().get(0))))
        {   return false;
        }
        if (!target.isEmpty())
        {
            if (!(entity instanceof MonsterEntity) || !target.test(req -> req.test(((MonsterEntity) entity).getTarget())))
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