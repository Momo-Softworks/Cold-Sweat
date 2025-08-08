package com.momosoftworks.coldsweat.api.temperature.modifier;

import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;

public class SoulSproutTempModifier extends FoodTempModifier
{
    public SoulSproutTempModifier()
    {   this(0);
    }

    public SoulSproutTempModifier(double effect)
    {   super(effect);
    }

    @Override
    public void tick(LivingEntity entity)
    {
        super.tick(entity);
        if (Math.random() < 0.3 && entity.tickCount % 5 == 0 && entity.level.isClientSide)
        {
            WorldHelper.spawnParticleBatch(entity.level, ParticleTypes.SOUL, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                                           entity.getBbWidth() / 2, entity.getBbHeight() / 2, entity.getBbWidth() / 2, 1, 0.02);
        }
    }
}
