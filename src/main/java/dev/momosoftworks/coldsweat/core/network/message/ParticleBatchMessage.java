package dev.momosoftworks.coldsweat.core.network.message;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ParticleBatchMessage
{
    Set<Pair<ParticleOptions, ParticlePlacement>> particles = new HashSet<>();

    public void addParticle(ParticleOptions particle, ParticlePlacement placement)
    {
        particles.add(Pair.of(particle, placement));
    }

    public static void encode(ParticleBatchMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.particles.size());
        for (Pair<ParticleOptions, ParticlePlacement> entry : message.particles)
        {
            String particleID = ForgeRegistries.PARTICLE_TYPES.getKey(entry.getFirst().getType()).toString();
            buffer.writeInt(particleID.length());
            buffer.writeCharSequence(particleID, StandardCharsets.UTF_8);
            entry.getFirst().writeToNetwork(buffer);
            buffer.writeNbt(entry.getSecond().toNBT());
        }
    }

    public static ParticleBatchMessage decode(FriendlyByteBuf buffer)
    {
        ParticleBatchMessage message = new ParticleBatchMessage();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
        {
            int particleIDLength = buffer.readInt();
            ParticleType type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(buffer.readCharSequence(particleIDLength, StandardCharsets.UTF_8).toString()));
            ParticleOptions particle = type.getDeserializer().fromNetwork(type, buffer);
            ParticlePlacement placement = ParticlePlacement.fromNBT(buffer.readNbt());
            message.addParticle(particle, placement);
        }

        return message;
    }

    public static void handle(ParticleBatchMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        context.enqueueWork(() ->
        {
            for (Pair<ParticleOptions, ParticlePlacement> entry : message.particles)
            {
                ParticleOptions particle = entry.getFirst();
                ParticlePlacement placement = entry.getSecond();
                Minecraft.getInstance().level.addParticle(particle, placement.x, placement.y, placement.z, placement.vx, placement.vy, placement.vz);
            }
        });
        context.setPacketHandled(true);
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
