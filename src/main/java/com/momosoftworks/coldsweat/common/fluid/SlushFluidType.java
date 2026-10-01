package com.momosoftworks.coldsweat.common.fluid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;


public class SlushFluidType extends FluidType
{
    public SlushFluidType(Properties properties)
    {   super(properties);
    }

    @Override
    public double motionScale(Entity entity)
    {   return 0.00235D;
    }

    @Override
    public void setItemMovement(ItemEntity entity)
    {
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(vec3.x * 0.95F, vec3.y + (vec3.y < 0.06F ? 5.0E-4F : 0.0F), vec3.z * 0.95F);
    }
}
