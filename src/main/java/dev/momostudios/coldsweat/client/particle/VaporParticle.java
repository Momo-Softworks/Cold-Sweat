package dev.momostudios.coldsweat.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class VaporParticle extends TextureSheetParticle
{
    private SpriteSet ageSprite;
    private boolean gravity;

    protected VaporParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, boolean gravity)
    {
        super(world, x, y, z);
        this.ageSprite = spriteSet;
        this.alpha = 0.0f;
        this.scale(0.4f + (float) (Math.random() / 2.5f));
        this.lifetime = 40;
        this.hasPhysics = true;
        this.setParticleSpeed(vx, vy, vz);
        this.setSpriteFromAge(spriteSet);
        this.gravity = gravity;
    }

    @Override
    public void move(double x, double y, double z)
    {
        if (x != 0.0D || y != 0.0D || z != 0.0D)
        {
            this.setBoundingBox(this.getBoundingBox().move(x, onGround ? 0 : y, z));
            AABB axisalignedbb = this.getBoundingBox();
            this.x = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
            this.y = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
            this.z = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {
        if (Minecraft.getInstance().options.particles == ParticleStatus.MINIMAL)
            this.remove();

        if (this.age++ >= this.getLifetime())
        {
            this.remove();
        }
        else
        {
            yd += gravity ? -0.04d : 0.04d;
            this.move(xd * (onGround ? 1 : 0.2), yd, zd * (onGround ? 1 : 0.2));
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }

        this.setSpriteFromAge(ageSprite);

        if (gravity)
        {
            if (this.age < 10)
                this.alpha += 0.02f;
            else if (this.age > 32)
                this.alpha -= 0.02f;

            if (this.alpha < 0.035 && this.age > 10)
                this.remove();
        }
        else
        {
            if (this.age < 10)
                this.alpha += 0.07f;
            else if (this.age > 32)
                this.alpha -= 0.02f;

            if (this.alpha < 0.07  && this.age > 10)
                this.remove();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record SteamFactory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL)
                return new VaporParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, false);
            else
                return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record MistFactory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType>
    {
        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            ParticleStatus status = Minecraft.getInstance().options.particles;
            if (status != ParticleStatus.MINIMAL && status != ParticleStatus.DECREASED)
                return new VaporParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, true);
            else
                return null;
        }
    }
}
