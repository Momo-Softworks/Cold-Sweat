package net.momostudios.coldsweat.common.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class ToleranceEffect extends Effect
{
    public ToleranceEffect() {
        super(EffectType.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName() {
        return "effect.tolerance";
    }

    public boolean isInstant() {
        return false;
    }

    public boolean shouldRenderInvText(EffectInstance effect) {
        return false;
    }

    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    public boolean shouldRenderHUD(EffectInstance effect) {
        return false;
    }
}
