package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ChameleonEatMessage
{
    int entityId;

    public ChameleonEatMessage(int entityId)
    {
        this.entityId = entityId;
    }

    public static void encode(ChameleonEatMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.entityId);
    }

    public static ChameleonEatMessage decode(PacketBuffer buffer)
    {
        return new ChameleonEatMessage(buffer.readInt());
    }

    public static void handle(ChameleonEatMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
            if (entity instanceof ChameleonEntity)
            {   ((ChameleonEntity) entity).eatAnimation();
            }
        });
        context.setPacketHandled(true);
    }
}
