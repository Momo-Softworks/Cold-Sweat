package dev.momostudios.coldsweat.core.init;

import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;

public class PotionInit
{
    public static final DeferredRegister<Potion> POTIONS;
    public static final RegistryObject<Potion> ICE_RESISTANCE_POTION;

    public PotionInit() {
    }

    static
    {
        POTIONS = DeferredRegister.create(ForgeRegistries.POTION_TYPES, ColdSweat.MOD_ID);
        ICE_RESISTANCE_POTION = POTIONS.register("ice_resistance", () -> new Potion(new EffectInstance(EffectInit.ICE_RESISTANCE_EFFECT_REGISTRY.get(), 9600)));
    }
}
