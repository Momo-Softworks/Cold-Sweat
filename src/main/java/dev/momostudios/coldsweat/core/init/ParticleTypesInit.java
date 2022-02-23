package dev.momostudios.coldsweat.core.init;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import dev.momostudios.coldsweat.ColdSweat;

public class ParticleTypesInit
{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ColdSweat.MOD_ID);

    public static final RegistryObject<BasicParticleType> HEARTH_AIR = PARTICLES.register("hearth_air", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> STEAM = PARTICLES.register("steam", () -> new BasicParticleType(true));
    public static final RegistryObject<BasicParticleType> MIST = PARTICLES.register("mist", () -> new BasicParticleType(true));
}
