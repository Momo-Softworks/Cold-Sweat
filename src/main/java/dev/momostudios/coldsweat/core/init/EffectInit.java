package dev.momostudios.coldsweat.core.init;

import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.effect.IceResistanceEffect;
import dev.momostudios.coldsweat.common.effect.InsulatedEffect;
import dev.momostudios.coldsweat.common.effect.GraceEffect;

public class EffectInit
{
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, ColdSweat.MOD_ID);

    public static final RegistryObject<Effect> INSULATED = EFFECTS.register("insulated", InsulatedEffect::new);
    public static final RegistryObject<Effect> GRACE = EFFECTS.register("grace", GraceEffect::new);
    public static final RegistryObject<Effect> ICE_RESISTANCE = EFFECTS.register("ice_resistance", IceResistanceEffect::new);
}
