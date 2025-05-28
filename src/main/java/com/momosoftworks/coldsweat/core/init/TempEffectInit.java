package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.DecreaseDropsEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.PreventBreedingEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.player.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.Objects;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class TempEffectInit
{
    public static Registry<TempEffectType<?>> TEMP_EFFECTS_REGISTRY;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event)
    {
        RegistryBuilder<TempEffectType<?>> registryBuilder = new RegistryBuilder<>(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "temp_effect")));
        TEMP_EFFECTS_REGISTRY = event.create(registryBuilder);
    }

    @SubscribeEvent
    public static void onRegistration(RegisterEvent event)
    {
        if (Objects.equals(event.getRegistry(), TEMP_EFFECTS_REGISTRY))
        {
            event.register((ResourceKey) event.getRegistryKey(), helper ->
            {
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_healing"), new TempEffectType<>(FreezeHealingEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_hearts"), new TempEffectType<>(FreezeHeartsEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_knockback"), new TempEffectType<>(FreezeKnockbackEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_mining_speed"), new TempEffectType<>(FreezeMineSpeedEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_movement_speed"), new TempEffectType<>(FreezeMoveSpeedEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_shiver"), new TempEffectType<>(FreezeShiverEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "freeze_overlay"), new TempEffectType<>(FreezeVignetteEffect::new));

                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_blur"), new TempEffectType<>(HeatBlurEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_fog"), new TempEffectType<>(HeatFogEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_sway"), new TempEffectType<>(HeatSwayEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "heat_vignette"), new TempEffectType<>(HeatVignetteEffect::new));

                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "prevent_breeding"), new TempEffectType<>(PreventBreedingEffect::new));
                helper.register(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "decrease_drops"), new TempEffectType<>(DecreaseDropsEffect::new));
            });
        }
    }
}
