package dev.momostudios.coldsweat.core.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraftforge.registries.RegistryObject;

public class PotionInit
{
    public static final DeferredRegister<Potion> POTIONS;
    public static final RegistryObject<Potion> ICE_RESISTANCE_POTION;

    public PotionInit() {
    }

    static
    {
        POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, ColdSweat.MOD_ID);
        ICE_RESISTANCE_POTION = POTIONS.register("ice_resistance", () -> new Potion(new MobEffectInstance(EffectInit.ICE_RESISTANCE_EFFECT_REGISTRY.get(), 9600)));
    }
}