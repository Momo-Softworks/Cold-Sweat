package com.momosoftworks.coldsweat.core.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraftforge.registries.RegistryObject;

public class ParticleTypesInit
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<SimpleParticleType> WARM_AIR = PARTICLES.register("warm_air", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> COLD_AIR = PARTICLES.register("cold_air", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SMOKESTACK_WARM = PARTICLES.register("smokestack_warm", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMOKESTACK_COLD = PARTICLES.register("smokestack_cold", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GROUND_MIST = PARTICLES.register("ground_mist", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> MOB_COLD = PARTICLES.register("mob_cold", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> MOB_HOT = PARTICLES.register("mob_hot", () -> new SimpleParticleType(true));
}
