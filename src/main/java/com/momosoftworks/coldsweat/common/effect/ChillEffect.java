package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public class ChillEffect extends MobEffect
{
    public ChillEffect()
    {   super(MobEffectCategory.BENEFICIAL, 8961252);
    }

    @Nonnull
    public String getName()
    {   return "effect.chill";
    }

    public boolean isInstant()
    {   return false;
    }
}
