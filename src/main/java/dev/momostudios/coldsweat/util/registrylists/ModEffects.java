package dev.momostudios.coldsweat.util.registrylists;

import dev.momostudios.coldsweat.core.init.EffectInit;
import net.minecraft.potion.Effect;

public class ModEffects
{
    public static Effect INSULATION = EffectInit.INSULATED_EFFECT_REGISTRY.get();
    public static Effect ICE_RESISTANCE = EffectInit.ICE_RESISTANCE_EFFECT_REGISTRY.get();
    public static Effect GRACE = EffectInit.GRACE_EFFECT_REGISTRY.get();
}
