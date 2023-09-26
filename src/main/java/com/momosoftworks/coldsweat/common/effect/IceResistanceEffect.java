package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public class IceResistanceEffect extends MobEffect
{
    public IceResistanceEffect() {
        super(MobEffectCategory.BENEFICIAL, 8109800);
    }

    @Nonnull
    public String getName() {
        return "effect.ice_resistance";
    }

    public boolean isInstant() {
        return false;
    }
}
