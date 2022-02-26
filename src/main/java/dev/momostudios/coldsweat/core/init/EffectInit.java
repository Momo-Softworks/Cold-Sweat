package dev.momostudios.coldsweat.core.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.effect.IceResistanceEffect;
import dev.momostudios.coldsweat.common.effect.InsulatedEffect;
import dev.momostudios.coldsweat.common.effect.GraceEffect;
import net.minecraftforge.registries.RegistryObject;

public class EffectInit
{
    public static final DeferredRegister<MobEffect> EFFECTS;
    public static final RegistryObject<MobEffect> INSULATED_EFFECT_REGISTRY;
    public static final RegistryObject<MobEffect> GRACE_EFFECT_REGISTRY;
    public static final RegistryObject<MobEffect> ICE_RESISTANCE_EFFECT_REGISTRY;

    public EffectInit() {
    }

    static
    {
        EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ColdSweat.MOD_ID);

        INSULATED_EFFECT_REGISTRY = EFFECTS.register("insulated", InsulatedEffect::new);
        GRACE_EFFECT_REGISTRY = EFFECTS.register("grace", GraceEffect::new);
        ICE_RESISTANCE_EFFECT_REGISTRY = EFFECTS.register("ice_resistance", IceResistanceEffect::new);
    }
}
