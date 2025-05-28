package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.DecreaseDropsEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.PreventBreedingEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.player.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class TempEffectInit
{
    public static final DeferredRegister<TempEffectType<?>> REGISTRY_REGISTER = DeferredRegister.create((Class) TempEffectType.class, ColdSweat.MOD_ID);
    public static final Supplier<IForgeRegistry<TempEffectType<?>>> TEMP_EFFECTS_REGISTRY = REGISTRY_REGISTER.makeRegistry("temp_effect", RegistryBuilder::new);

    static
    {
        REGISTRY_REGISTER.register("freeze_healing", () -> new TempEffectType<>(FreezeHealingEffect::new));
        REGISTRY_REGISTER.register("freeze_hearts", () -> new TempEffectType<>(FreezeHeartsEffect::new));
        REGISTRY_REGISTER.register("freeze_knockback", () -> new TempEffectType<>(FreezeKnockbackEffect::new));
        REGISTRY_REGISTER.register("freeze_mining_speed", () -> new TempEffectType<>(FreezeMineSpeedEffect::new));
        REGISTRY_REGISTER.register("freeze_movement_speed", () -> new TempEffectType<>(FreezeMoveSpeedEffect::new));
        REGISTRY_REGISTER.register("freeze_shiver", () -> new TempEffectType<>(FreezeShiverEffect::new));
        REGISTRY_REGISTER.register("freeze_overlay", () -> new TempEffectType<>(FreezeVignetteEffect::new));

        REGISTRY_REGISTER.register("heat_blur", () -> new TempEffectType<>(HeatBlurEffect::new));
        REGISTRY_REGISTER.register("heat_fog", () -> new TempEffectType<>(HeatFogEffect::new));
        REGISTRY_REGISTER.register("heat_sway", () -> new TempEffectType<>(HeatSwayEffect::new));
        REGISTRY_REGISTER.register("heat_vignette", () -> new TempEffectType<>(HeatVignetteEffect::new));

        REGISTRY_REGISTER.register("prevent_breeding", () -> new TempEffectType<>(PreventBreedingEffect::new));
        REGISTRY_REGISTER.register("decrease_drops", () -> new TempEffectType<>(DecreaseDropsEffect::new));
    }
}
