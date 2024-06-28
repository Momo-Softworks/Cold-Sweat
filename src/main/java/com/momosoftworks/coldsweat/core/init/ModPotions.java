package com.momosoftworks.coldsweat.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import com.momosoftworks.coldsweat.ColdSweat;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions
{
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ColdSweat.MOD_ID);

    public static final DeferredHolder<Potion, Potion> ICE_RESISTANCE = POTIONS.register("ice_resistance", () ->
            new Potion(new MobEffectInstance(ModEffects.ICE_RESISTANCE, 1800)));
    public static final DeferredHolder<Potion, Potion> LONG_ICE_RESISTANCE = POTIONS.register("long_ice_resistance", () ->
            new Potion("ice_resistance", new MobEffectInstance(ModEffects.ICE_RESISTANCE, 4800)));
}
