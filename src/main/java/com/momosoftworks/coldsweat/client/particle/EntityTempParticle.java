package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityTempParticle extends TextureSheetParticle
{
    public EntityTempParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.86F;
        this.xd *= 0.01F;
        this.yd *= 0.01F;
        this.zd *= 0.01F;
        this.yd += 0.1D;
        this.quadSize *= 1.5F;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType()
    {   return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float scale)
    {   return this.quadSize * Mth.clamp((this.age + scale) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            EntityTempParticle particle = new EntityTempParticle(level, x, y, z);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
