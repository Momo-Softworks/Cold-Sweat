package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DummyEntity extends MobEntity
{
    public DummyEntity(World level)
    {   super(ModEntities.CHAMELEON, level);
        this.setPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ());
    }

    @Override
    public boolean isSpectator()
    {   return false;
    }
}
