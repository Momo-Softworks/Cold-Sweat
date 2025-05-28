package com.momosoftworks.coldsweat.api.temperature.effect;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Objects;

public abstract class TempEffect
{
    private final LivingEntity entity;
    private final IntegerBounds bounds;

    public TempEffect(LivingEntity entity, IntegerBounds range)
    {
        this.entity = entity;
        this.bounds = range;
        if (FMLEnvironment.dist == Dist.CLIENT || !this.isClient())
        {   NeoForge.EVENT_BUS.register(this);
        }
    }

    protected abstract boolean isClient();

    protected LivingEntity entity()
    {   return this.entity;
    }
    protected IntegerBounds bounds()
    {   return this.bounds;
    }

    protected boolean test(Entity entity)
    {   return Objects.equals(this.entity, entity) && this.bounds().test((int) this.getTemperature());
    }

    protected double getTemperature()
    {   return Temperature.get(entity, Temperature.Trait.BODY);
    }
}