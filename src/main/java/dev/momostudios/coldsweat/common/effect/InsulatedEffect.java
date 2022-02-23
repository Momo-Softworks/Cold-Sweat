package dev.momostudios.coldsweat.common.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class InsulatedEffect extends Effect
{
    public InsulatedEffect() {
        super(EffectType.BENEFICIAL, 7355178);
    }

    @Nonnull
    public String getName() {
        return "effect.insulated";
    }

    public boolean isInstant() {
        return false;
    }
}
