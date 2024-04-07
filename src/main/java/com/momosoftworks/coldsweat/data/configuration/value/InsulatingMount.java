package com.momosoftworks.coldsweat.data.configuration.value;

import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.util.serialization.NbtSerializable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class InsulatingMount implements NbtSerializable
{
    public EntityType<?> entityType;
    public double coldInsulation; 
    public double heatInsulation; 
    public EntityRequirement requirement;
    
    public InsulatingMount(EntityType<?> entityType, double coldInsulation, double heatInsulation, EntityRequirement requirement)
    {
        this.entityType = entityType;
        this.coldInsulation = coldInsulation;
        this.heatInsulation = heatInsulation;
        this.requirement = requirement;
    }
    public boolean test(Entity entity)
    {   return requirement.test(entity);
    }

    @Override
    public CompoundNBT serialize()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("entity", ForgeRegistries.ENTITIES.getKey(entityType).toString());
        tag.putDouble("cold_insulation", coldInsulation);
        tag.putDouble("heat_insulation", heatInsulation);
        tag.put("requirement", requirement.serialize());
        return tag;
    }

    public static InsulatingMount deserialize(CompoundNBT tag)
    {
        return new InsulatingMount(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(tag.getString("entity"))),
                                   tag.getDouble("cold_insulation"),
                                   tag.getDouble("heat_insulation"),
                                   EntityRequirement.deserialize(tag.getCompound("requirement")));
    }
}
