package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncPreferredUnitsMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncPreferredUnitsMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_preferred_units"));
    public static final StreamCodec<FriendlyByteBuf, SyncPreferredUnitsMessage> CODEC = CustomPacketPayload.codec(SyncPreferredUnitsMessage::encode, SyncPreferredUnitsMessage::decode);

    Temperature.Units units;

    public SyncPreferredUnitsMessage(Temperature.Units units)
    {   this.units = units;
    }

    public static void encode(SyncPreferredUnitsMessage message, FriendlyByteBuf buffer)
    {   buffer.writeEnum(message.units);
    }

    public static SyncPreferredUnitsMessage decode(FriendlyByteBuf buffer)
    {    return new SyncPreferredUnitsMessage(buffer.readEnum(Temperature.Units.class));
    }

    public static void handle(SyncPreferredUnitsMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {   EntityTempManager.getTemperatureCap(context.player()).ifPresent(cap -> cap.setPreferredUnits(message.units));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}