package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class ParticleBatchMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ParticleBatchMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(ColdSweat.MOD_ID, "particle_batch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleBatchMessage> CODEC = CustomPacketPayload.codec(ParticleBatchMessage::encode, ParticleBatchMessage::decode);

    private static final BinaryOperator<Vec3> MIN_POS_COMPARATOR = (a, b) -> new Vec3(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
    private static final BinaryOperator<Vec3> MAX_POS_COMPARATOR = (a, b) -> new Vec3(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));

    Set<Pair<ParticleOptions, ParticlePlacement>> particles = new HashSet<>();
    int minSetting;

    /**
     * @param minSetting The minimum particle setting for the particles to render.<br>
     * 0: All<br>
     * 1: Decreased<br>
     * 2: Minimal<br>
     */
    public ParticleBatchMessage(int minSetting)
    {   this.minSetting = minSetting;
    }

    public ParticleBatchMessage()
    {   this(-1);
    }

    public ParticleBatchMessage addParticle(ParticleOptions particle, ParticlePlacement placement)
    {   particles.add(Pair.of(particle, placement));
        return this;
    }

    public ParticleBatchMessage addParticle(ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
    {   addParticle(particle, new ParticlePlacement(x, y, z, xSpeed, ySpeed, zSpeed));
        return this;
    }

    public void sendEntity(Entity entity)
    {
        if (particles.isEmpty() || entity.level().isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, this);
    }

    public void sendWorld(Level level)
    {
        if (particles.isEmpty() || !(level instanceof ServerLevel serverLevel)) return;
        Vec3 minPos = particles.stream().map(Pair::getSecond).map(p -> new Vec3(p.x, p.y, p.z)).reduce(MIN_POS_COMPARATOR).get();
        Vec3 maxPos = particles.stream().map(Pair::getSecond).map(p -> new Vec3(p.x, p.y, p.z)).reduce(MAX_POS_COMPARATOR).get();
        Vec3 midPos = minPos.add(maxPos).scale(0.5);
        PacketDistributor.sendToPlayersNear(serverLevel, null, midPos.x, midPos.y, midPos.z, minPos.distanceTo(maxPos) + 32, this);
    }

    public static void encode(ParticleBatchMessage message, RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(message.minSetting);
        buffer.writeInt(message.particles.size());
        for (Pair<ParticleOptions, ParticlePlacement> entry : message.particles)
        {
            ParticleTypes.STREAM_CODEC.encode(buffer, entry.getFirst());
            buffer.writeNbt(entry.getSecond().toNBT());
        }
    }

    public static ParticleBatchMessage decode(RegistryFriendlyByteBuf buffer)
    {
        ParticleBatchMessage message = new ParticleBatchMessage(buffer.readInt());
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            ParticleOptions particle = ParticleTypes.STREAM_CODEC.decode(buffer);
            ParticlePlacement placement = ParticlePlacement.fromNBT(buffer.readNbt());
            message.addParticle(particle, placement);
        }

        return message;
    }

    public static void handle(ParticleBatchMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            for (Pair<ParticleOptions, ParticlePlacement> entry : message.particles)
            {
                ParticleOptions particle = entry.getFirst();
                ParticlePlacement placement = entry.getSecond();

                if (message.minSetting == -1 || Minecraft.getInstance().options.particles().get().ordinal() <= message.minSetting)
                {
                    ClientOnlyHelper.getClientLevel().addParticle(particle, placement.x, placement.y, placement.z, placement.vx, placement.vy, placement.vz);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static class ParticlePlacement
    {
        double x, y, z, vx, vy, vz;

        public ParticlePlacement(double x, double y, double z, double vx, double vy, double vz)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
        }

        public CompoundTag toNBT()
        {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", x);
            tag.putDouble("y", y);
            tag.putDouble("z", z);
            tag.putDouble("vx", vx);
            tag.putDouble("vy", vy);
            tag.putDouble("vz", vz);
            return tag;
        }

        public static ParticlePlacement fromNBT(CompoundTag tag)
        {
            return new ParticlePlacement(
                    tag.getDoubleOr("x", 0),
                    tag.getDoubleOr("y", 0),
                    tag.getDoubleOr("z", 0),
                    tag.getDoubleOr("vx", 0),
                    tag.getDoubleOr("vy", 0),
                    tag.getDoubleOr("vz", 0)
            );
        }
    }
}