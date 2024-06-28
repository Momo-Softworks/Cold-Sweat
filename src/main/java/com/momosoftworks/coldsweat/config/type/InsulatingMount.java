package com.momosoftworks.coldsweat.config.type;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record InsulatingMount(EntityType<?> entityType, double coldInsulation, double heatInsulation, EntityRequirement requirement) implements NbtSerializable
{
    public boolean test(Entity entity)
    {   return requirement.test(entity);
    }

    @Override
    public CompoundTag serialize()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("entity", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        tag.putDouble("cold_insulation", coldInsulation);
        tag.putDouble("heat_insulation", heatInsulation);
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static InsulatingMount deserialize(CompoundTag tag)
    {
        return new InsulatingMount(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("entity"))),
                                   tag.getDouble("cold_insulation"),
                                   tag.getDouble("heat_insulation"),
                                   EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
