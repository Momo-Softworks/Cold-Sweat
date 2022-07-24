package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerTempSyncMessage
{
    public double core;
    public double base;
    public double world;
    public double max;
    public double min;
    public boolean instantBody;

    public PlayerTempSyncMessage(double world, double core, double base, double max, double min, boolean instantBody)
    {
        this.world = world;
        this.core = core;
        this.base = base;
        this.max = max;
        this.min = min;
        this.instantBody = instantBody;
    }

    public static void encode(PlayerTempSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.world);
        buffer.writeDouble(message.core);
        buffer.writeDouble(message.base);
        buffer.writeDouble(message.max);
        buffer.writeDouble(message.min);
        buffer.writeBoolean(message.instantBody);
    }

    public static PlayerTempSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerTempSyncMessage(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readBoolean());
    }

    public static void handle(PlayerTempSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null)
                {
                    player.getCapability(ModCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                    {
                        cap.set(Temperature.Type.WORLD, message.world);
                        cap.set(Temperature.Type.CORE, message.core);
                        cap.set(Temperature.Type.BASE, message.base);
                        cap.set(Temperature.Type.MAX, message.max);
                        cap.set(Temperature.Type.MIN, message.min);
                        if (message.instantBody)
                        {
                            Overlays.setBodyTemp(message.base + message.core);
                        }
                    });
                }
            });
        }

        context.setPacketHandled(true);
    }
}
