package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPreferredUnitsMessage
{
    Temperature.Units units;

    public SyncPreferredUnitsMessage(Temperature.Units units)
    {   this.units = units;
    }

    public static void encode(SyncPreferredUnitsMessage message, PacketBuffer buffer)
    {   buffer.writeEnum(message.units);
    }

    public static SyncPreferredUnitsMessage decode(PacketBuffer buffer)
    {    return new SyncPreferredUnitsMessage(buffer.readEnum(Temperature.Units.class));
    }

    public static void handle(SyncPreferredUnitsMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isServer())
            {
                if (context.getSender() != null)
                {   EntityTempManager.getTemperatureCap(context.getSender()).ifPresent(cap -> cap.setPreferredUnits(message.units));
                }
            }
        });
        context.setPacketHandled(true);
    }
}
