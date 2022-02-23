package dev.momostudios.coldsweat.client.particle;

import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.ColdSweat;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleUtil
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particles.registerFactory(ParticleTypesInit.HEARTH_AIR.get(), HearthParticle.Factory::new);
        Minecraft.getInstance().particles.registerFactory(ParticleTypesInit.STEAM.get(), SteamParticle.SteamFactory::new);
        Minecraft.getInstance().particles.registerFactory(ParticleTypesInit.MIST.get(), SteamParticle.MistFactory::new);
    }
}
