package dev.momostudios.coldsweat.common.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class GraceEffect extends Effect
{
    public GraceEffect() {
        super(EffectType.BENEFICIAL, 7355178);
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
