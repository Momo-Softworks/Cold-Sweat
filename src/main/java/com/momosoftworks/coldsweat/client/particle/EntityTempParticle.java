package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class EntityTempParticle extends SingleQuadParticle
{
    public EntityTempParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprite);
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
    protected Layer getLayer()
    {   return Layer.OPAQUE;
    }

    @Override
    public float getQuadSize(float scale)
    {   return this.quadSize * Mth.clamp((this.age + scale) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
        {
            return new EntityTempParticle(level, x, y, z, sprite.get(random));
        }
    }
}
