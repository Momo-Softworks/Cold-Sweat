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
    protected HearthParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite spriteSet)
    {
        super(world, x, y, z);
        float size = 0.5f;
        this.ageSprite = spriteSet;

        this.alpha = 0.0f;
        this.setSize(size, size);
        this.scale(3f + (float) (Math.random()));
        this.lifetime = 40;
        this.gravity = 0;
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
    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        public final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {   return new HearthParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite);
        }
    }
}