package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.data.codec.requirement.sub_type.EntitySubRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EntityRequirement(Optional<EntityType<?>> type, Optional<TagKey<EntityType<?>>> tag,
                                Optional<LocationRequirement> location, Optional<LocationRequirement> steppingOn,
                                Optional<EffectsRequirement> effects, Optional<NbtRequirement> nbt, Optional<EntityFlagsRequirement> flags,
                                Optional<EquipmentRequirement> equipment, Optional<EntitySubRequirement> typeSpecificData,
                                Optional<String> team,
                                Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger, Optional<EntityRequirement> target)
{
    public static EntityRequirement NONE = new EntityRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty());

    public static Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
            TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("tag").forGetter(requirement -> requirement.tag),
            LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
            LocationRequirement.CODEC.optionalFieldOf("stepping_on").forGetter(requirement -> requirement.steppingOn),
            EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment),
            Codec.STRING.optionalFieldOf("team").forGetter(requirement -> requirement.team)
    ).apply(instance, (type, tag, location, standingOn, effects, nbt, flags, equipment, team) -> new EntityRequirement(type, tag, location, standingOn, effects, nbt, flags, equipment,
                                                                                                            Optional.empty(), team, Optional.empty(), Optional.empty(), Optional.empty())));

    private static final List<Codec<EntityRequirement>> REQUIREMENT_CODEC_STACK = new ArrayList<>(List.of(SIMPLE_CODEC));
    // Allow for up to 16 layers of inner codecs
    static
    {   for (int i = 0; i < 4; i++)
        {   addCodecStack();
        }
    }

    public static Codec<EntityRequirement> getCodec()
    {   return REQUIREMENT_CODEC_STACK.getLast();
    }

    private static void addCodecStack()
    {
        var latestCodec = REQUIREMENT_CODEC_STACK.getLast();
        var codec = RecordCodecBuilder.<EntityRequirement>create(instance -> instance.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
                TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("tag").forGetter(requirement -> requirement.tag),
                LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
                LocationRequirement.CODEC.optionalFieldOf("stepping_on").forGetter(requirement -> requirement.steppingOn),
                EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment),
                EntitySubRequirement.CODEC.optionalFieldOf("type_specific").forGetter(requirement -> requirement.typeSpecificData),
                Codec.STRING.optionalFieldOf("team").forGetter(requirement -> requirement.team),
                latestCodec.optionalFieldOf("vehicle").forGetter(requirement -> requirement.vehicle),
                latestCodec.optionalFieldOf("passenger").forGetter(requirement -> requirement.passenger),
                latestCodec.optionalFieldOf("targeted_entity").forGetter(requirement -> requirement.target)
        ).apply(instance, EntityRequirement::new));

        REQUIREMENT_CODEC_STACK.add(codec);
    }

    public boolean test(Entity entity)
    {
        if (entity == null)
        {   return true;
        }
        if (Objects.equals(this, NONE))
        {   return true;
        }
        if (type.isPresent() && !type.get().equals(entity.getType()))
        {   return false;
        }
        if (tag.isPresent() && !entity.getType().is(tag.get()))
        {   return false;
        }
        if (location.isPresent() && !location.get().test(entity.level(), entity.position()))
        {   return false;
        }
        if (steppingOn.isPresent() && !steppingOn.get().test(entity.level(), entity.position().add(0, -0.5, 0)))
        {   return false;
        }
        if (effects.isPresent() && !effects.get().test(entity))
        {   return false;
        }
        if (nbt.isPresent() && !nbt.get().test(entity))
        {   return false;
        }
        if (flags.isPresent() && !flags.get().test(entity))
        {   return false;
        }
        if (equipment.isPresent() && !equipment.get().test(entity))
        {   return false;
        }
        if (typeSpecificData.isPresent() && !typeSpecificData.get().test(entity, entity.level(), entity.position()))
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
        if (team.isPresent())
        {
            Team team = entity.getTeam();
            if (team == null || !team.getName().equals(this.team.get()))
            {   return false;
            }
        }
        return true;
    }

    public CompoundTag serialize()
    {   return (CompoundTag) getCodec().encodeStart(NbtOps.INSTANCE, this).result().orElseGet(CompoundTag::new);
    }

    public static EntityRequirement deserialize(CompoundTag tag)
    {   return getCodec().decode(NbtOps.INSTANCE, tag).result().orElseThrow(() -> new IllegalArgumentException("Could not deserialize EntityRequirement")).getFirst();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        EntityRequirement that = (EntityRequirement) obj;

        return type.equals(that.type)
            && location.equals(that.location)
            && steppingOn.equals(that.steppingOn)
            && effects.equals(that.effects)
            && nbt.equals(that.nbt)
            && flags.equals(that.flags)
            && equipment.equals(that.equipment)
            && typeSpecificData.equals(that.typeSpecificData)
            && vehicle.equals(that.vehicle)
            && passenger.equals(that.passenger)
            && target.equals(that.target);
    }

    @Override
    public String toString()
    {   return getCodec().encodeStart(JsonOps.INSTANCE, this).result().map(Object::toString).orElse("serialize_failed");
    }
}