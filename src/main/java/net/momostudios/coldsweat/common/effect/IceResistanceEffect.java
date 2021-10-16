package net.momostudios.coldsweat.common.effect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

import javax.annotation.Nonnull;

public class IceResistanceEffect extends Effect
{
    public IceResistanceEffect() {
        super(EffectType.BENEFICIAL, 8109800);
    }

    @Nonnull
    public String getName() {
        return "effect.ice_resistance";
    }

    public boolean isInstant() {
        return false;
    }
}
