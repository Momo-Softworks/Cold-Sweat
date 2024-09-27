package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EntityRequirement(Optional<EntityType<?>> type, Optional<TagKey<EntityType<?>>> tag,
                                Optional<LocationRequirement> location, Optional<LocationRequirement> steppingOn,
                                Optional<EffectsRequirement> effects, Optional<NbtRequirement> nbt, Optional<EntityFlagsRequirement> flags,
                                Optional<EquipmentRequirement> equipment, Optional<PlayerDataRequirement> playerData,
                                Optional<EntityRequirement> vehicle, Optional<EntityRequirement> passenger, Optional<EntityRequirement> target)
{
    public static EntityRequirement NONE = new EntityRequirement(Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty(),
                                                                 Optional.empty(), Optional.empty(), Optional.empty());

    public static Codec<EntityRequirement> SIMPLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ForgeRegistries.ENTITY_TYPES.getCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
            TagKey.codec(Registry.ENTITY_TYPE_REGISTRY).optionalFieldOf("tag").forGetter(requirement -> requirement.tag),
            LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
            LocationRequirement.CODEC.optionalFieldOf("stepping_on").forGetter(requirement -> requirement.steppingOn),
            EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
            NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
            EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
            EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment)
    ).apply(instance, (type, tag, location, standingOn, effects, nbt, flags, equipment) -> new EntityRequirement(type, tag, location, standingOn, effects, nbt, flags, equipment,
                                                                                                            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())));

    private static final List<Codec<EntityRequirement>> REQUIREMENT_CODEC_STACK = new ArrayList<>(List.of(SIMPLE_CODEC));
    // Allow for up to 16 layers of inner codecs
    static
    {   for (int i = 0; i < 16; i++)
        {   addCodecStack();
        }
    }

    public static Codec<EntityRequirement> getCodec()
    {   return REQUIREMENT_CODEC_STACK.get(REQUIREMENT_CODEC_STACK.size() - 1);
    }

    private static void addCodecStack()
    {
        var latestCodec = REQUIREMENT_CODEC_STACK.get(REQUIREMENT_CODEC_STACK.size() - 1);
        var codec = RecordCodecBuilder.<EntityRequirement>create(instance -> instance.group(
                ForgeRegistries.ENTITY_TYPES.getCodec().optionalFieldOf("type").forGetter(requirement -> requirement.type),
                TagKey.codec(Registry.ENTITY_TYPE_REGISTRY).optionalFieldOf("tag").forGetter(requirement -> requirement.tag),
                LocationRequirement.CODEC.optionalFieldOf("location").forGetter(requirement -> requirement.location),
                LocationRequirement.CODEC.optionalFieldOf("stepping_on").forGetter(requirement -> requirement.steppingOn),
                EffectsRequirement.CODEC.optionalFieldOf("effects").forGetter(requirement -> requirement.effects),
                NbtRequirement.CODEC.optionalFieldOf("nbt").forGetter(requirement -> requirement.nbt),
                EntityFlagsRequirement.CODEC.optionalFieldOf("flags").forGetter(requirement -> requirement.flags),
                EquipmentRequirement.CODEC.optionalFieldOf("equipment").forGetter(requirement -> requirement.equipment),
                PlayerDataRequirement.getCodec(latestCodec).optionalFieldOf("player_data").forGetter(requirement -> requirement.playerData),
                latestCodec.optionalFieldOf("vehicle").forGetter(requirement -> requirement.vehicle),
                latestCodec.optionalFieldOf("passenger").forGetter(requirement -> requirement.passenger),
                latestCodec.optionalFieldOf("target").forGetter(requirement -> requirement.target)
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
        if (location.isPresent() && !location.get().test(entity.level, entity.position()))
        {   return false;
        }
        if (steppingOn.isPresent() && !steppingOn.get().test(entity.level, entity.position().add(0, -0.5, 0)))
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
        if (playerData.isPresent() && !playerData.get().test(entity))
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
            if (!(entity instanceof Monster monster))
            {   return false;
            }
            if (!target.get().test(monster.getTarget()))
            {   return false;
            }
        }
        return true;
    }

    public CompoundTag serialize()
    {
        try
        {   CompoundTag compound = new CompoundTag();
            type.ifPresent(type -> compound.putString("type", ForgeRegistries.ENTITY_TYPES.getKey(type).toString()));
            tag.ifPresent(tag -> compound.putString("tag", tag.location().toString()));
            location.ifPresent(location -> compound.put("location", location.serialize()));
            steppingOn.ifPresent(standingOn -> compound.put("standing_on", standingOn.serialize()));
            effects.ifPresent(effects -> compound.put("effects", effects.serialize()));
            nbt.ifPresent(nbt -> compound.put("nbt", nbt.serialize()));
            flags.ifPresent(flags -> compound.put("flags", flags.serialize()));
            equipment.ifPresent(equipment -> compound.put("equipment", equipment.serialize()));
            playerData.ifPresent(playerData -> compound.put("player_data", playerData.serialize()));
            vehicle.ifPresent(vehicle -> compound.put("vehicle", vehicle.serialize()));
            passenger.ifPresent(passenger -> compound.put("passenger", passenger.serialize()));
            target.ifPresent(target -> compound.put("target", target.serialize()));
            return compound;
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Error serializing entity requirement: {}", e.getMessage());
            e.printStackTrace();
            return new CompoundTag();
        }
    }

    public static EntityRequirement deserialize(CompoundTag compound)
    {
        if (compound.isEmpty()) return NONE;
        try
        {   Optional<EntityType<?>> type = compound.contains("type") ? Optional.of(ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(compound.getString("type")))) : Optional.empty();
            Optional<TagKey<EntityType<?>>> tag = compound.contains("tag") ? Optional.of(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(compound.getString("tag")))) : Optional.empty();
            Optional<LocationRequirement> location = compound.contains("location") ? Optional.of(LocationRequirement.deserialize(compound.getCompound("location"))) : Optional.empty();
            Optional<LocationRequirement> standingOn = compound.contains("standing_on") ? Optional.of(LocationRequirement.deserialize(compound.getCompound("standing_on"))) : Optional.empty();
            Optional<EffectsRequirement> effects = compound.contains("effects") ? Optional.of(EffectsRequirement.deserialize(compound.getCompound("effects"))) : Optional.empty();
            Optional<NbtRequirement> nbt = compound.contains("nbt") ? Optional.of(NbtRequirement.deserialize(compound.getCompound("nbt"))) : Optional.empty();
            Optional<EntityFlagsRequirement> flags = compound.contains("flags") ? Optional.of(EntityFlagsRequirement.deserialize(compound.getCompound("flags"))) : Optional.empty();
            Optional<EquipmentRequirement> equipment = compound.contains("equipment") ? Optional.of(EquipmentRequirement.deserialize(compound.getCompound("equipment"))) : Optional.empty();
            Optional<PlayerDataRequirement> playerData = compound.contains("player_data") ? Optional.of(PlayerDataRequirement.deserialize(compound.getCompound("player_data"))) : Optional.empty();
            Optional<EntityRequirement> vehicle = compound.contains("vehicle") ? Optional.of(EntityRequirement.deserialize(compound.getCompound("vehicle"))) : Optional.empty();
            Optional<EntityRequirement> passenger = compound.contains("passenger") ? Optional.of(EntityRequirement.deserialize(compound.getCompound("passenger"))) : Optional.empty();
            Optional<EntityRequirement> target = compound.contains("target") ? Optional.of(EntityRequirement.deserialize(compound.getCompound("target"))) : Optional.empty();

            return new EntityRequirement(type, tag, location, standingOn, effects, nbt, flags, equipment, playerData, vehicle, passenger, target);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Error deserializing entity requirement: {}", e.getMessage());
            e.printStackTrace();
            return NONE;
        }
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
            && playerData.equals(that.playerData)
            && vehicle.equals(that.vehicle)
            && passenger.equals(that.passenger)
            && target.equals(that.target);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("EntityRequirement{");
        type.ifPresent(type -> builder.append("type=").append(type).append(", "));
        location.ifPresent(location -> builder.append("location=").append(location).append(", "));
        steppingOn.ifPresent(standingOn -> builder.append("standing_on=").append(standingOn).append(", "));
        effects.ifPresent(effects -> builder.append("effects=").append(effects).append(", "));
        nbt.ifPresent(nbt -> builder.append("nbt=").append(nbt).append(", "));
        flags.ifPresent(flags -> builder.append("flags=").append(flags).append(", "));
        equipment.ifPresent(equipment -> builder.append("equipment=").append(equipment).append(", "));
        playerData.ifPresent(playerData -> builder.append("player_data=").append(playerData).append(", "));
        vehicle.ifPresent(vehicle -> builder.append("vehicle=").append(vehicle).append(", "));
        passenger.ifPresent(passenger -> builder.append("passenger=").append(passenger).append(", "));
        target.ifPresent(target -> builder.append("target=").append(target).append(", "));
        builder.append('}');

        return builder.toString();
    }
}
