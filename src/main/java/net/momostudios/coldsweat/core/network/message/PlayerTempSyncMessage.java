package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.util.PlayerHelper;

import java.util.function.Supplier;

public class PlayerTempSyncMessage
{
    public double body;
    public double base;

    public PlayerTempSyncMessage() {
    }

    public PlayerTempSyncMessage(double body, double base){
        this.body = body;
        this.base = base;
    }

    public static void encode(PlayerTempSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeDouble(message.body);
        buffer.writeDouble(message.base);
    }

    public static PlayerTempSyncMessage decode(PacketBuffer buffer)
    {
        return new PlayerTempSyncMessage(buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(PlayerTempSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            Entity ent = Minecraft.getInstance().getRenderViewEntity();
            if (ent instanceof ClientPlayerEntity && Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isSpectator())
            {
                ClientPlayerEntity player = (ClientPlayerEntity) ent;

                player.getCapability(PlayerTempCapability.TEMPERATURE).ifPresent(cap ->
                {
                    cap.set(PlayerHelper.Types.BODY, message.body);
                    cap.set(PlayerHelper.Types.BASE, message.base);
                    cap.set(PlayerHelper.Types.COMPOSITE, message.body + message.base);
                });
            }
        });
        context.setPacketHandled(true);
    }
}
