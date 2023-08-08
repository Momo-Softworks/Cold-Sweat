package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableHearthParticlesMessage
{
    CompoundNBT nbt;
    int entityID;
    String worldKey;

    public DisableHearthParticlesMessage(PlayerEntity player, CompoundNBT nbt)
    {
        this.nbt = nbt;
        this.entityID = player.getId();
        this.worldKey = player.level.dimension().location().toString();
    }

    DisableHearthParticlesMessage(int entityID, String worldKey, CompoundNBT nbt)
    {
        this.nbt = nbt;
        this.entityID = entityID;
        this.worldKey = worldKey;
    }

    public static void encode(DisableHearthParticlesMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entityID);
        buffer.writeUtf(message.worldKey);
        buffer.writeNbt(message.nbt);
    }

    public static DisableHearthParticlesMessage decode(PacketBuffer buffer)
    {
        return new DisableHearthParticlesMessage(buffer.readInt(), buffer.readUtf(), buffer.readNbt());
    }

    public static void handle(DisableHearthParticlesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            try
            {
                World world = (context.getDirection().getReceptionSide().isClient() && ClientOnlyHelper.getClientWorld().dimension().location().toString().equals(message.worldKey))
                        ? ClientOnlyHelper.getClientWorld()
                        : ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(message.worldKey)));
                if (world != null)
                {
                    Entity entity = world.getEntity(message.entityID);
                    if (entity instanceof PlayerEntity)
                    {   entity.getPersistentData().put("disabledHearths", message.nbt);
                    }
                }
            } catch (Exception ignored) {}
        });
        context.setPacketHandled(true);
    }
}
