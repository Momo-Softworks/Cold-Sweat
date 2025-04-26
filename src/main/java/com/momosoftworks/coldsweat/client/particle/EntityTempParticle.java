package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

public class EntityTempParticle extends SpriteTexturedParticle
{
    public EntityTempParticle(ClientWorld level, double x, double y, double z) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.xd *= 0.01;
        this.yd *= 0.01;
        this.zd *= 0.01;
        this.yd += 0.1D;
        this.quadSize *= 1.5F;
        this.lifetime = 16;
        this.hasPhysics = false;
    }

    @Override
    public IParticleRenderType getRenderType()
    {   return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float scale)
    {   return this.quadSize * MathHelper.clamp((this.age + scale) / this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Factory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite)
        {   this.sprite = sprite;
        }

        public IAnimatedSprite sprite()
        {   return sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            EntityTempParticle particle = new EntityTempParticle(level, x, y, z);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
