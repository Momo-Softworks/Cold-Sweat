package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTempModifiersMessage
{
    int entityId;
    CompoundNBT modifiers;

    public SyncTempModifiersMessage(LivingEntity entity, CompoundNBT modifiers)
    {
        this.entityId = entity.getId();
        this.modifiers = modifiers;
    }

    SyncTempModifiersMessage(int entityId, CompoundNBT modifiers)
    {
        this.entityId = entityId;
        this.modifiers = modifiers;
    }

    public static void encode(SyncTempModifiersMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.modifiers);
    }

    public static SyncTempModifiersMessage decode(PacketBuffer buffer)
    {
        return new SyncTempModifiersMessage(buffer.readInt(), buffer.readNbt());
    }

    public static void handle(SyncTempModifiersMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
        context.enqueueWork(() ->
        {
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);

            if (entity instanceof LivingEntity)
            {
                EntityTempManager.getTemperatureCap(((LivingEntity) entity)).ifPresent(cap ->
                {   cap.deserializeModifiers(message.modifiers);
                });
            }
        });

        context.setPacketHandled(true);
    }
}