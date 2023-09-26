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

    public static final RegistryObject<SimpleParticleType> HEARTH_AIR = PARTICLES.register("hearth_air", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> STEAM = PARTICLES.register("steam", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> MIST = PARTICLES.register("mist", () -> new SimpleParticleType(true));
}
