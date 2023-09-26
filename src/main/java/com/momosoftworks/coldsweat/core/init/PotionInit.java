package com.momosoftworks.coldsweat.core.init;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.momosoftworks.coldsweat.ColdSweat;

public class PotionInit
{
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<Potion> ICE_RESISTANCE = POTIONS.register("ice_resistance", () ->
            new Potion(new EffectInstance(EffectInit.ICE_RESISTANCE.get(), 1800)));
    public static final RegistryObject<Potion> ICE_RESISTANCE_LONG = POTIONS.register("ice_resistance_long", () ->
            new Potion("ice_resistance", new EffectInstance(EffectInit.ICE_RESISTANCE.get(), 4800)));
}
