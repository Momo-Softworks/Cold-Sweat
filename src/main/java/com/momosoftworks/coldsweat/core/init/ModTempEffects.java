package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.DecreaseDropsEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.PreventBreedingEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.player.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTempEffects
{
    public static final DeferredRegister<TempEffectType<?>> TEMP_EFFECTS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "temp_effect"), ColdSweat.MOD_ID);
    public static final Registry<TempEffectType<?>> REGISTRY = TEMP_EFFECTS.makeRegistry(builder -> {});

    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_HEALING = TEMP_EFFECTS.register("freeze_healing", () -> new TempEffectType<>(FreezeHealingEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_HEARTS = TEMP_EFFECTS.register("freeze_hearts", () -> new TempEffectType<>(FreezeHeartsEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_KNOCKBACK = TEMP_EFFECTS.register("freeze_knockback", () -> new TempEffectType<>(FreezeKnockbackEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_MINING_SPEED = TEMP_EFFECTS.register("freeze_mining_speed", () -> new TempEffectType<>(FreezeMineSpeedEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_MOVEMENT_SPEED = TEMP_EFFECTS.register("freeze_movement_speed", () -> new TempEffectType<>(FreezeMoveSpeedEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_SHIVER = TEMP_EFFECTS.register("freeze_shiver", () -> new TempEffectType<>(FreezeShiverEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> FREEZE_OVERLAY = TEMP_EFFECTS.register("freeze_overlay", () -> new TempEffectType<>(FreezeVignetteEffect::new));

    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> HEAT_BLUR = TEMP_EFFECTS.register("heat_blur", () -> new TempEffectType<>(HeatBlurEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> HEAT_FOG = TEMP_EFFECTS.register("heat_fog", () -> new TempEffectType<>(HeatFogEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> HEAT_SWAY = TEMP_EFFECTS.register("heat_sway", () -> new TempEffectType<>(HeatSwayEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> HEAT_OVERLAY = TEMP_EFFECTS.register("heat_vignette", () -> new TempEffectType<>(HeatVignetteEffect::new));

    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> PREVENT_BREEDING = TEMP_EFFECTS.register("prevent_breeding", () -> new TempEffectType<>(PreventBreedingEffect::new));
    public static final DeferredHolder<TempEffectType<?>, TempEffectType<?>> DECREASE_DROPS = TEMP_EFFECTS.register("decrease_drops", () -> new TempEffectType<>(DecreaseDropsEffect::new));
}
