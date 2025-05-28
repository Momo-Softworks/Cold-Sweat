package com.momosoftworks.coldsweat.core.init;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.DecreaseDropsEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.entity.PreventBreedingEffect;
import com.momosoftworks.coldsweat.api.temperature.effect.player.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

import java.util.Objects;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TempEffectInit
{
    public static Supplier<IForgeRegistry<TempEffectType<?>>> TEMP_EFFECTS_REGISTRY;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event)
    {
        RegistryBuilder<TempEffectType<?>> registryBuilder = new RegistryBuilder<TempEffectType<?>>().setName(new ResourceLocation(ColdSweat.MOD_ID, "temp_effect"));
        TEMP_EFFECTS_REGISTRY = event.create(registryBuilder);
    }

    @SubscribeEvent
    public static void onRegistration(RegisterEvent event)
    {
        if (Objects.equals(event.getForgeRegistry(), TEMP_EFFECTS_REGISTRY.get()))
        {
            event.register((ResourceKey) event.getRegistryKey(), helper ->
            {
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_healing"), new TempEffectType<>(FreezeHealingEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_hearts"), new TempEffectType<>(FreezeHeartsEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_knockback"), new TempEffectType<>(FreezeKnockbackEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_mining_speed"), new TempEffectType<>(FreezeMineSpeedEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_movement_speed"), new TempEffectType<>(FreezeMoveSpeedEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_shiver"), new TempEffectType<>(FreezeShiverEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "freeze_overlay"), new TempEffectType<>(FreezeVignetteEffect::new));

                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "heat_blur"), new TempEffectType<>(HeatBlurEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "heat_fog"), new TempEffectType<>(HeatFogEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "heat_sway"), new TempEffectType<>(HeatSwayEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "heat_vignette"), new TempEffectType<>(HeatVignetteEffect::new));

                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "prevent_breeding"), new TempEffectType<>(PreventBreedingEffect::new));
                helper.register(new ResourceLocation(ColdSweat.MOD_ID, "decrease_drops"), new TempEffectType<>(DecreaseDropsEffect::new));
            });
        }
    }
}
