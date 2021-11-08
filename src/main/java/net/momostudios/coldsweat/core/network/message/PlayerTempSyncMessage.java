package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.core.util.PlayerTemp;

import java.util.function.Supplier;

public class PlayerTempSyncMessage
{
    public float body;
    public float base;

    public PlayerTempSyncMessage() {
    }

    public PlayerTempSyncMessage(float body, float base) {
        this.body = body;
        this.base = base;
    }

    public static void encode(PlayerTempSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeFloat(message.body);
        buffer.writeFloat(message.base);
    }

    public static PlayerTempSyncMessage decode(PacketBuffer buffer)
    {
        return new PlayerTempSyncMessage(buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(PlayerTempSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            ClientPlayerEntity player = Minecraft.getInstance().player;

            if (player != null)
            player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
            {
                cap.set(PlayerTemp.Types.BODY, message.body);
                cap.set(PlayerTemp.Types.BASE, message.base);
            });
        });
        context.setPacketHandled(true);
    }
}
