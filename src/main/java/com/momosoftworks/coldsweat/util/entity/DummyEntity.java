package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.util.registries.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class DummyEntity extends Mob
{
    public DummyEntity(Level level)
    {   super(ModEntities.CHAMELEON, level);
        this.setPos(BlockPos.ZERO.getX(), BlockPos.ZERO.getY(), BlockPos.ZERO.getZ());
    }

    @Override
    public boolean isSpectator()
    {   return false;
    }
}
