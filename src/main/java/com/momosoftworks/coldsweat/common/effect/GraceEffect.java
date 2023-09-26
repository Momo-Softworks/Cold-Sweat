package com.momosoftworks.coldsweat.common.effect;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import javax.annotation.Nonnull;

public class GraceEffect extends MobEffect
{
    public GraceEffect() {
        super(MobEffectCategory.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName() {
        return "effect.grace";
    }

    public boolean isInstant() {
        return false;
    }

    public boolean shouldRenderInvText(EffectInstance effect) {
        return true;
    }

    public boolean shouldRender(EffectInstance effect) {
        return true;
    }

    public boolean shouldRenderHUD(EffectInstance effect) {
        return true;
    }
}
