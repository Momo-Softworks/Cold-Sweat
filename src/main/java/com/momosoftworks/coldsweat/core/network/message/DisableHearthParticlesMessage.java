package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableHearthParticlesMessage
{
    CompoundTag nbt;

    public DisableHearthParticlesMessage(CompoundTag nbt)
    {   this.nbt = nbt;
    }

    public static void encode(DisableHearthParticlesMessage message, FriendlyByteBuf buffer)
    {   buffer.writeNbt(message.nbt);
    }

    public static DisableHearthParticlesMessage decode(FriendlyByteBuf buffer)
    {   return new DisableHearthParticlesMessage(buffer.readNbt());
    }

    public static void handle(DisableHearthParticlesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getSender() != null && context.getDirection().getReceptionSide() == LogicalSide.SERVER)
        {
            context.enqueueWork(() ->
            {   context.getSender().getPersistentData().put("DisabledHearths", message.nbt);
            });
        }
        else if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            context.enqueueWork(() ->
            {   HearthSaveDataHandler.deserializeDisabledHearths(message.nbt);
            });
        }
        context.setPacketHandled(true);
    }
}
