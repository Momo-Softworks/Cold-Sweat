package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HearthParticle extends SpriteTexturedParticle
{
    private IAnimatedSprite ageSprite;

    protected HearthParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite spriteSet, ParticleType type)
    {
        super(world, x, y, z);
        float size = 0.5f;
        this.ageSprite = spriteSet;

        this.setSize(size, size);
        this.scale(3f + (float) (Math.random()));
        this.lifetime = 40;
        switch (type)
        {
            case WARM_AIR:
            {   this.gravity = 0;
                this.alpha = 0.1f;
                break;
            }
            case SMOKESTACK:
            {   this.gravity = -0.05f;
                this.alpha = 0.25f;
                break;
            }
        }
        this.hasPhysics = true;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {   super.tick();
        this.setSpriteFromAge(this.ageSprite);

        if (this.age < 10)
            this.alpha += 0.02f;
        else if (this.age > 32)
            this.alpha -= 0.02f;

        if (this.alpha <= 0.06  && this.age > 10)
            this.remove();
    }

    @OnlyIn(Dist.CLIENT)
    public static class AirParticleFactory implements IParticleFactory<BasicParticleType>
    {
        public final IAnimatedSprite sprite;

        public AirParticleFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {   return new HearthParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, ParticleType.WARM_AIR);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SmokestackFactory implements IParticleFactory<BasicParticleType>
    {
        public final IAnimatedSprite sprite;

        public SmokestackFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            if (ySpeed == 0) ySpeed = 0.04f;
            return new HearthParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, ParticleType.SMOKESTACK);
        }
    }

    public enum ParticleType
    {
        WARM_AIR,
        SMOKESTACK
    }
}