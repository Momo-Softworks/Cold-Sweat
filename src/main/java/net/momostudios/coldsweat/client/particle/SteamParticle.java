package net.momostudios.coldsweat.client.particle;

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

import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class SteamParticle extends SpriteTexturedParticle
{
    private IAnimatedSprite ageSprite;
    private boolean collidedY;
    private boolean gravity;

    protected SteamParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite spriteSet, boolean gravity)
    {
        super(world, x, y, z);
        this.ageSprite = spriteSet;
        this.particleAlpha = 0.0f;
        this.particleScale = 0.4f + (float) (Math.random() / 2.5f);
        this.maxAge = 40;
        this.canCollide = true;
        this.motionX = vx * 1;
        this.motionY = vy * 1;
        this.motionZ = vz * 1;
        this.particleGravity = gravity ? 0.03f : -0.03f;
        this.selectSpriteWithAge(spriteSet);
        this.gravity = gravity;
    }

    @Override
    public void move(double x, double y, double z)
    {
        double d0 = x;
        double d1 = y;
        double d2 = z;
        if (this.canCollide && (x != 0.0D || y != 0.0D || z != 0.0D)) {
            Vector3d vector3d = Entity.collideBoundingBoxHeuristically((Entity)null, new Vector3d(x, y, z), this.getBoundingBox(), this.world, ISelectionContext.dummy(), new ReuseableStream<>(Stream.empty()));
            x = vector3d.x;
            y = vector3d.y;
            z = vector3d.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().offset(x, collidedY ? 0 : y, z));
            AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
            this.posY = axisalignedbb.minY + (gravity ? 0.2 : 0);
            this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        }

        if (Math.abs(d1) >= 1.0E-5d && Math.abs(y) < 1.0E-5d) {
            this.collidedY = true;
        }

        this.onGround = d1 != y && d1 < 0.0D;
        if (d0 != x) {
            this.motionX = 0.0D;
        }

        if (d2 != z) {
            this.motionZ = 0.0D;
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick()
    {
        if (Minecraft.getInstance().gameSettings.particles == ParticleStatus.MINIMAL)
            this.setExpired();

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge)
        {
            this.setExpired();
        }
        else
        {
            this.motionY -= 0.04D * particleGravity;
            this.move(motionX * (onGround ? 1 : 0.2), motionY, motionZ * (onGround ? 1 : 0.2));
            this.motionX *= 0.99;
            this.motionY *= 0.99;
            this.motionZ *= 0.99;
        }

        this.selectSpriteWithAge(ageSprite);

        if (gravity)
        {
            if (this.age < 10)
                this.particleAlpha += 0.02f;
            else if (this.age > 32)
                this.particleAlpha -= 0.02f;

            if (this.particleAlpha < 0.035 && this.age > 10)
                this.setExpired();
        }
        else
        {
            if (this.age < 10)
                this.particleAlpha += 0.07f;
            else if (this.age > 32)
                this.particleAlpha -= 0.02f;

            if (this.particleAlpha < 0.07  && this.age > 10)
                this.setExpired();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SteamFactory implements IParticleFactory<BasicParticleType>
    {
        public final IAnimatedSprite sprite;

        public SteamFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            if (Minecraft.getInstance().gameSettings.particles != ParticleStatus.MINIMAL)
                return new SteamParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, false);
            else
                return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MistFactory implements IParticleFactory<BasicParticleType>
    {
        public final IAnimatedSprite sprite;

        public MistFactory(IAnimatedSprite sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ParticleStatus status = Minecraft.getInstance().gameSettings.particles;
            if (status != ParticleStatus.MINIMAL && status != ParticleStatus.DECREASED)
                return new SteamParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite, true);
            else
                return null;
        }
    }
}
