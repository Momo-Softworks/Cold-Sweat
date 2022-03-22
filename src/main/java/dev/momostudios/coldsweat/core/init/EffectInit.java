package dev.momostudios.coldsweat.core.init;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.common.effect.IceResistanceEffect;
import dev.momostudios.coldsweat.common.effect.InsulatedEffect;
import dev.momostudios.coldsweat.common.effect.GraceEffect;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;

public class EffectInit
{
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ColdSweat.MOD_ID);

    public static final RegistryObject<MobEffect> INSULATED = EFFECTS.register("insulated", InsulatedEffect::new);
    public static final RegistryObject<MobEffect> GRACE = EFFECTS.register("grace", GraceEffect::new);
    public static final RegistryObject<MobEffect> ICE_RESISTANCE = EFFECTS.register("ice_resistance", IceResistanceEffect::new);
}
