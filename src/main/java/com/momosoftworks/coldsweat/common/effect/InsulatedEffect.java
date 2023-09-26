package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public class InsulatedEffect extends MobEffect
{
    public InsulatedEffect() {
        super(MobEffectCategory.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName() {
        return "effect.insulated";
    }

    public boolean isInstant() {
        return false;
    }
}
