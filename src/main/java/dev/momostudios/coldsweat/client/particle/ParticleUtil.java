package dev.momostudios.coldsweat.client.particle;

import dev.momostudios.coldsweat.core.init.ParticleTypesInit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.ColdSweat;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleUtil
{
    @SubscribeEvent
    public static void registerParticles(ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.HEARTH_AIR.get(), HearthParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.STEAM.get(), VaporParticle.SteamFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.MIST.get(), VaporParticle.MistFactory::new);
    }
}
