package dev.momostudios.coldsweat.util.registrylists;

import dev.momostudios.coldsweat.core.init.EffectInit;
import net.minecraft.world.effect.MobEffect;

public class ModEffects
{
    public static MobEffect INSULATION = EffectInit.INSULATED_EFFECT_REGISTRY.get();
    public static MobEffect ICE_RESISTANCE = EffectInit.ICE_RESISTANCE_EFFECT_REGISTRY.get();
    public static MobEffect GRACE = EffectInit.GRACE_EFFECT_REGISTRY.get();
}
