package net.momostudios.coldsweat.core.init;

import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.common.effect.InsulatedEffect;

@Mod.EventBusSubscriber
public class EffectInit
{
    public static final DeferredRegister<Effect> EFFECTS;
    public static final RegistryObject<Effect> INSULATED_EFFECT_REGISTRY;

    public EffectInit() {
    }

    static
    {
        EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, ColdSweat.MOD_ID);
        INSULATED_EFFECT_REGISTRY = EFFECTS.register("insulated", InsulatedEffect::new);
    }
}
