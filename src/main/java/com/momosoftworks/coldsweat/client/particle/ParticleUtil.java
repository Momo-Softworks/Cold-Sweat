package com.momosoftworks.coldsweat.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.momosoftworks.coldsweat.core.init.ModParticleTypes;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleUtil
{
    public static ParticleRenderType PARTICLE_SHEET_TRANSPARENT = new ParticleRenderType()
    {
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager)
        {   RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        public String toString()
        {   return "PARTICLE_SHEET_TRANSPARENT";
        }
    };

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event)
    {   event.registerSpriteSet(ModParticleTypes.HEARTH_AIR.get(), HearthParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.STEAM.get(), VaporParticle.SteamFactory::new);
        event.registerSpriteSet(ModParticleTypes.GROUND_MIST.get(), VaporParticle.GroundMistFactory::new);
        event.registerSpriteSet(ModParticleTypes.MIST.get(), VaporParticle.MistFactory::new);
    }
}
