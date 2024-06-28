package com.momosoftworks.coldsweat.core.network.message;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public class ParticleBatchMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ParticleBatchMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "particle_batch"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleBatchMessage> CODEC = CustomPacketPayload.codec(ParticleBatchMessage::encode, ParticleBatchMessage::decode);

    Set<Pair<ParticleOptions, ParticlePlacement>> particles = new HashSet<>();

    public void addParticle(ParticleOptions particle, ParticlePlacement placement)
    {
        particles.add(Pair.of(particle, placement));
    }

    public static void encode(ParticleBatchMessage message, RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(message.particles.size());
        for (Pair<ParticleOptions, ParticlePlacement> entry : message.particles)
        {
            ParticleTypes.STREAM_CODEC.encode(buffer, entry.getFirst());
            buffer.writeNbt(entry.getSecond().toNBT());
        }
    }

    public static ParticleBatchMessage decode(RegistryFriendlyByteBuf buffer)
    {
        ParticleBatchMessage message = new ParticleBatchMessage();
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
                Minecraft.getInstance().level.addParticle(particle, placement.x, placement.y, placement.z, placement.vx, placement.vy, placement.vz);
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
                    tag.getDouble("x"),
                    tag.getDouble("y"),
                    tag.getDouble("z"),
                    tag.getDouble("vx"),
                    tag.getDouble("vy"),
                    tag.getDouble("vz")
            );
        }
    }
}