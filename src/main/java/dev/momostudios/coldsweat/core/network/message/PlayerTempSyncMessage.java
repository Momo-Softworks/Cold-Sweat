package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerTempSyncMessage
{
    CompoundTag temps;
    boolean instant;

    public PlayerTempSyncMessage(CompoundTag temps, boolean instant)
    {
        this.temps = temps;
        this.instant = instant;
    }

    public static void encode(PlayerTempSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(message.temps);
        buffer.writeBoolean(message.instant);
    }

    public static PlayerTempSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new PlayerTempSyncMessage(buffer.readNbt(), buffer.readBoolean());
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
                        if (cap instanceof PlayerTempCap playerTempCap)
                        {
                            playerTempCap.deserializeTemps(message.temps);

                            if (message.instant)
                            {
                                Overlays.setBodyTemp(cap.getTemp(Temperature.Type.BODY));
                            }
                        }
                    });
                }
            });
        }

        context.setPacketHandled(true);
    }
}
