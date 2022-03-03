package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.capability.CSCapabilities;
import dev.momostudios.coldsweat.common.temperature.Temperature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerTempSyncMessage
{
    public double body;
    public double base;
    public double ambient;

    public PlayerTempSyncMessage() {
    }

    public PlayerTempSyncMessage(double body, double base, double ambient){
        this.body = body;
        this.base = base;
        this.ambient = ambient;
    }

    public static void encode(PlayerTempSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.body);
        buffer.writeDouble(message.base);
        buffer.writeDouble(message.ambient);
    }

    public static PlayerTempSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerTempSyncMessage(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(PlayerTempSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> syncTemperature(message.body, message.base, message.ambient)));

        context.setPacketHandled(true);
    }

    public static DistExecutor.SafeRunnable syncTemperature(double body, double base, double ambient)
    {
        return new DistExecutor.SafeRunnable()
        {
            @Override
            public void run()
            {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null && !player.isSpectator())
                {
                    player.getCapability(CSCapabilities.PLAYER_TEMPERATURE).ifPresent(cap ->
                    {
                        cap.set(Temperature.Types.BODY, body);
                        cap.set(Temperature.Types.BASE, base);
                        cap.set(Temperature.Types.COMPOSITE, body + base);
                        cap.set(Temperature.Types.AMBIENT, ambient);
                    });
                }
            }
        };
    }
}
