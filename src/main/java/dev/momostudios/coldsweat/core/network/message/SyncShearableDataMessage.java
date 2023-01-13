package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncShearableDataMessage
{
    private final boolean isSheared;
    private final int lastSheared;
    private final int entityId;

    public SyncShearableDataMessage(boolean isSheared, int lastSheared, int entityId)
    {
        this.isSheared = isSheared;
        this.lastSheared = lastSheared;
        this.entityId = entityId;
    }

    public static void encode(SyncShearableDataMessage msg, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(msg.isSheared);
        buffer.writeInt(msg.lastSheared);
        buffer.writeInt(msg.entityId);
    }

    public static SyncShearableDataMessage decode(FriendlyByteBuf buffer)
    {
        return new SyncShearableDataMessage(buffer.readBoolean(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(SyncShearableDataMessage msg, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                Level level = Minecraft.getInstance().level;
                if (level != null)
                {
                    Entity entity = level.getEntity(msg.entityId);
                    if (entity != null)
                    {
                        entity.getCapability(ModCapabilities.SHEARABLE_FUR).ifPresent(cap ->
                        {
                            cap.setSheared(msg.isSheared);
                            cap.setLastSheared(msg.lastSheared);
                        });
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
