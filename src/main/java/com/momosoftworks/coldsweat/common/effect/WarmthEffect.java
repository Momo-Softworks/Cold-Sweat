package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public class WarmthEffect extends MobEffect
{
    public WarmthEffect()
    {   super(MobEffectCategory.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName()
    {   return "effect.warmth";
    }

    public boolean isInstant()
    {   return false;
    }
}
