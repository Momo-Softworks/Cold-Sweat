package com.momosoftworks.coldsweat.core.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticleTypes
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, ColdSweat.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WARM_AIR = PARTICLES.register("warm_air", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> COLD_AIR = PARTICLES.register("cold_air", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKESTACK_WARM = PARTICLES.register("smokestack_warm", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SMOKESTACK_COLD = PARTICLES.register("smokestack_cold", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GROUND_MIST = PARTICLES.register("ground_mist", () -> new SimpleParticleType(true));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOB_COLD = PARTICLES.register("mob_cold", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MOB_HOT = PARTICLES.register("mob_hot", () -> new SimpleParticleType(true));
}
