package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.ShearableFurManager;
import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncShearableDataMessage
{
    private final boolean isSheared;
    private final int lastSheared;
    private final int entityId;

    public SyncShearableDataMessage(boolean isSheared, int lastSheared, int entityId)
    {   this.isSheared = isSheared;
        this.lastSheared = lastSheared;
        this.entityId = entityId;
    }

    public static void encode(SyncShearableDataMessage msg, PacketBuffer buffer)
    {   buffer.writeBoolean(msg.isSheared);
        buffer.writeInt(msg.lastSheared);
        buffer.writeInt(msg.entityId);
    }

    public static SyncShearableDataMessage decode(PacketBuffer buffer)
    {   return new SyncShearableDataMessage(buffer.readBoolean(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(SyncShearableDataMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                try
                {
                    World world = ClientOnlyHelper.getClientWorld();
                    if (world != null)
                    {   Entity entity = world.getEntity(message.entityId);
                        if (entity instanceof LivingEntity)
                        {
                            ShearableFurManager.getFurCap(((LivingEntity) entity)).ifPresent(cap ->
                            {   cap.setSheared(message.isSheared);
                                cap.setLastSheared(message.lastSheared);
                            });
                        }
                    }
                } catch (Exception ignored) {}
            });
        }
        context.setPacketHandled(true);
    }
}
