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
    public double ambient;
    public double body;
    public double base;

    public PlayerTempSyncMessage() {
    }

    public PlayerTempSyncMessage(double ambient, double body, double base) {
        this.ambient = ambient;
        this.body = body;
        this.base = base;
    }

    public static void encode(PlayerTempSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeDouble(message.ambient);
        buffer.writeDouble(message.body);
        buffer.writeDouble(message.base);
    }

    public static PlayerTempSyncMessage decode(PacketBuffer buffer)
    {
        return new PlayerTempSyncMessage(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
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
                cap.set(PlayerTemp.Types.AMBIENT, message.ambient);
                cap.set(PlayerTemp.Types.BODY, message.body);
                cap.set(PlayerTemp.Types.BASE, message.base);
            });
        });
        context.setPacketHandled(true);
    }
}
