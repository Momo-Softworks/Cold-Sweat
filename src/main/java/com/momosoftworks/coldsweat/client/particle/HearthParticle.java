package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.util.RandomSource;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class HearthParticle extends SingleQuadParticle
{
    private SpriteSet ageSprite;
    VaporParticle.ParticleType type;

    protected HearthParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, ParticleType type)
    {
        super(world, x, y, z, spriteSet.first());
        float size = 0.5f;
        this.ageSprite = spriteSet;

        this.setSize(size, size);
        this.scale(3f + (float) Math.random());
        this.lifetime = 40;
        switch (type)
        {
            case WARM_AIR ->
            {   this.gravity = -0.01f;
                this.alpha = 0.1f;
            }
            case SMOKESTACK ->
            {   this.gravity = -0.05f;
                this.alpha = 0.25f;
            }
        }
        this.hasPhysics = true;
        this.setParticleSpeed(vx, vy, vz);
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    protected Layer getLayer()
    {   return Layer.TRANSLUCENT;
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

    public record AirParticleFactory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
        {   return new HearthParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, ParticleType.WARM_AIR);
        }
    }

    public record SmokestackFactory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
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
