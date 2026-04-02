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
    {   Minecraft.getInstance().particleEngine.register(ParticleTypesInit.WARM_AIR.get(), HearthParticle.AirParticleFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.COLD_AIR.get(), VaporParticle.MistFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.SMOKESTACK_WARM.get(), HearthParticle.SmokestackFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.SMOKESTACK_COLD.get(), VaporParticle.SmokestackFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.GROUND_MIST.get(), VaporParticle.GroundMistFactory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.MOB_COLD.get(), EntityTempParticle.Factory::new);
        Minecraft.getInstance().particleEngine.register(ParticleTypesInit.MOB_HOT.get(), EntityTempParticle.Factory::new);
    }
}
