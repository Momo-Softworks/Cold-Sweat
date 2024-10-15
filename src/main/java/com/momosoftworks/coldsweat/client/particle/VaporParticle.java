package com.momosoftworks.coldsweat.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class VaporParticle extends SpriteTexturedParticle
{
    private final IAnimatedSprite ageSprite;
    private final boolean hasGravity;
    private boolean collidedY;
    private float maxAlpha;
    VaporType type;

    protected VaporParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite spriteSet, VaporType type)
    {
        super(world, x, y, z);
        this.ageSprite = spriteSet;
        this.alpha = 0.0f;
        this.maxAlpha = (float) (Math.random() / 3 + 0.5f);
        this.scale(3f + (float) (Math.random() / 2.5f));
        this.setSize(quadSize / 10f, quadSize / 10f);
        this.lifetime = 40 + (int) (Math.random() * 20 - 10);
        this.hasPhysics = true;
        this.setSpriteFromAge(spriteSet);
        this.hasGravity = type == VaporType.GROUND_MIST;
        this.type = type;
        switch (type)
        {
            case STEAM : gravity = -0.04f; break;
            case GROUND_MIST : gravity = 0.04f; break;
            case MIST : gravity = 0f; break;
        }
        if (type == VaporType.MIST)
            this.maxAlpha = 0.2f;
    }

    @Nonnull
    @Override
    public IParticleRenderType getRenderType()
    {   return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {
        if (Minecraft.getInstance().options.particles == ParticleStatus.MINIMAL)
        {   this.remove();
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime)
        {   this.remove();
        }
        else
        {   this.yd -= 0.04D * gravity;
            this.move(xd * (onGround ? 1 : 0.2), yd, zd * (onGround ? 1 : 0.2));
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.xd *= 0.99;
        }

        this.setSpriteFromAge(ageSprite);

        if (type == VaporType.GROUND_MIST)
        {
            if (this.alpha < maxAlpha)
                this.alpha += 0.02f;
            else if (this.age > 32)
                this.alpha -= 0.02f;

            if (this.alpha < 0.035 && this.age > 10)
                this.remove();
        }
        else if (type == VaporType.MIST || type == VaporType.STEAM)
        {
            if (this.alpha < maxAlpha)
                this.alpha += maxAlpha / 20;
            else if (this.age > maxAlpha / (maxAlpha / 20))
                this.alpha -= maxAlpha / 20;

            if (this.alpha < 0.02 && this.age > 10)
                this.remove();
        }
    }

    @Override
    public void move(double x, double y, double z)
    {
        double d0 = x;
        double d1 = y;
        double d2 = z;
        if (this.hasPhysics && (x != 0.0D || y != 0.0D || z != 0.0D)) {
            Vector3d vec3 = Entity.collideBoundingBox(new Vector3d(x, y, z), this.getBoundingBox(), this.level, ISelectionContext.empty(), new ReuseableStream<>(Stream.of()));
            x = vec3.x;
            y = vec3.y;
            z = vec3.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(x, collidedY ? 0 : y, z));
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.x = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
            this.y = axisalignedbb.minY + (hasGravity ? 0.2 : 0);
            this.z = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        }

        if (Math.abs(d1) >= 1.0E-5d && Math.abs(y) < 1.0E-5d) {
            this.collidedY = true;
        }

        this.onGround = d1 != y && d1 < 0.0D;
        if (d0 != x) {
            this.xd = 0.0D;
        }

        if (d2 != z) {
            this.zd = 0.0D;
        }
    }

    public enum VaporType
    {
        STEAM,
        GROUND_MIST,
        MIST
    }

    @OnlyIn(Dist.CLIENT)
    public static class SteamFactory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite sprite;

        public SteamFactory(IAnimatedSprite spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            if (Minecraft.getInstance().options.particles != ParticleStatus.MINIMAL)
                return new VaporParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, VaporType.STEAM);
            else
                return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GroundMistFactory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite sprite;

        public GroundMistFactory(IAnimatedSprite spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {   return new VaporParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, VaporType.GROUND_MIST);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MistFactory implements IParticleFactory<BasicParticleType>
    {
        private final IAnimatedSprite sprite;

        public MistFactory(IAnimatedSprite spriteSet)
        {   this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {   return new VaporParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, VaporType.MIST);
        }
    }
}
