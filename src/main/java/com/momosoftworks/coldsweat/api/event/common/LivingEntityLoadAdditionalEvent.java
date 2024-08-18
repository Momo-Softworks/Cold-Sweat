package com.momosoftworks.coldsweat.api.event.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.eventbus.api.Event;

public class LivingEntityLoadAdditionalEvent extends Event
{
    private final LivingEntity entity;
    private final CompoundNBT nbt;

    public LivingEntityLoadAdditionalEvent(LivingEntity entity, CompoundNBT nbt)
    {
        this.entity = entity;
        this.nbt = nbt;
    }

    public LivingEntity getEntity()
    {   return entity;
    }

    public CompoundNBT getNBT()
    {   return nbt;
    }
}
