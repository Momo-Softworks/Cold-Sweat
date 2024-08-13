package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class WarmthEffect extends Effect
{
    public WarmthEffect()
    {   super(EffectType.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName()
    {   return "effect.warmth";
    }

    public boolean isInstant()
    {   return false;
    }
}
