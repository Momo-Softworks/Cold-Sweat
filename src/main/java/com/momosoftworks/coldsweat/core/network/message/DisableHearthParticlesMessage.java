package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.event.HearthSaveDataHandler;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DisableHearthParticlesMessage
{
    CompoundNBT nbt;

    public DisableHearthParticlesMessage(CompoundNBT nbt)
    {   this.nbt = nbt;
    }

    public static void encode(DisableHearthParticlesMessage message, PacketBuffer buffer)
    {   buffer.writeNbt(message.nbt);
    }

    public static DisableHearthParticlesMessage decode(PacketBuffer buffer)
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
