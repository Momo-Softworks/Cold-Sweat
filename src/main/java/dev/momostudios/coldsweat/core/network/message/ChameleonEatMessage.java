package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.entity.ChameleonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChameleonEatMessage
{
    int entityId;

    public ChameleonEatMessage(int entityId)
    {
        this.entityId = entityId;
    }

    public static void encode(ChameleonEatMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
    }

    public static ChameleonEatMessage decode(FriendlyByteBuf buffer)
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
            {
                ((ChameleonEntity) entity).eatAnimation();
            }
        });
        context.setPacketHandled(true);
    }
}
