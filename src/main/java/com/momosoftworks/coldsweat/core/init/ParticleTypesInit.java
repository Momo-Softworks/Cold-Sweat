package com.momosoftworks.coldsweat.core.init;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import com.momosoftworks.coldsweat.ColdSweat;

public class ParticleTypesInit
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<BasicParticleType> WARM_AIR = PARTICLES.register("warm_air", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> COLD_AIR = PARTICLES.register("cold_air", () -> new BasicParticleType(true));

    public static final RegistryObject<BasicParticleType> SMOKESTACK_WARM = PARTICLES.register("smokestack_warm", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> SMOKESTACK_COLD = PARTICLES.register("smokestack_cold", () -> new BasicParticleType(true));

    public static final RegistryObject<BasicParticleType> GROUND_MIST = PARTICLES.register("ground_mist", () -> new BasicParticleType(true));

    public static final RegistryObject<BasicParticleType> MOB_COLD = PARTICLES.register("mob_cold", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> MOB_HOT = PARTICLES.register("mob_hot", () -> new BasicParticleType(true));
}