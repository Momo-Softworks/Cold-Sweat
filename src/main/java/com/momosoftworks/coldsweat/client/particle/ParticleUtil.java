package com.momosoftworks.coldsweat.client.particle;

import com.momosoftworks.coldsweat.core.init.ParticleTypesInit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
