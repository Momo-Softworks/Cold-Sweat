package com.momosoftworks.coldsweat.client.particle;

import com.momosoftworks.coldsweat.core.init.ModParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ParticleUtil
{
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event)
    {   event.registerSpriteSet(ModParticleTypes.WARM_AIR.get(), HearthParticle.AirParticleFactory::new);
        event.registerSpriteSet(ModParticleTypes.COLD_AIR.get(), VaporParticle.MistFactory::new);
        event.registerSpriteSet(ModParticleTypes.SMOKESTACK_WARM.get(), HearthParticle.SmokestackFactory::new);
        event.registerSpriteSet(ModParticleTypes.SMOKESTACK_COLD.get(), VaporParticle.SmokestackFactory::new);
        event.registerSpriteSet(ModParticleTypes.GROUND_MIST.get(), VaporParticle.GroundMistFactory::new);
        event.registerSpriteSet(ModParticleTypes.MOB_COLD.get(), EntityTempParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.MOB_HOT.get(), EntityTempParticle.Factory::new);
    }
}
