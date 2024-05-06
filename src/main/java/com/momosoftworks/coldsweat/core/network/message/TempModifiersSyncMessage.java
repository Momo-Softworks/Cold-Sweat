package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TempModifiersSyncMessage
{
    int entityId;
    CompoundNBT modifiers;

    public TempModifiersSyncMessage(LivingEntity entity, CompoundNBT modifiers)
    {
        this.entityId = entity.getId();
        this.modifiers = modifiers;
    }

    TempModifiersSyncMessage(int entityId, CompoundNBT modifiers)
    {
        this.entityId = entityId;
        this.modifiers = modifiers;
    }

    public static void encode(TempModifiersSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.modifiers);
    }

    public static TempModifiersSyncMessage decode(PacketBuffer buffer)
    {
        return new TempModifiersSyncMessage(buffer.readInt(), buffer.readNbt());
    }

    public static void handle(TempModifiersSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
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