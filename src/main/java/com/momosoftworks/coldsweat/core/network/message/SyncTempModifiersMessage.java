package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTempModifiersMessage
{
    int entityId;
    CompoundTag modifiers;

    public SyncTempModifiersMessage(LivingEntity entity, CompoundTag modifiers)
    {
        this.entityId = entity.getId();
        this.modifiers = modifiers;
    }

    SyncTempModifiersMessage(int entityId, CompoundTag modifiers)
    {
        this.entityId = entityId;
        this.modifiers = modifiers;
    }

    public static void encode(SyncTempModifiersMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.modifiers);
    }

    public static SyncTempModifiersMessage decode(FriendlyByteBuf buffer)
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

            if (entity instanceof LivingEntity living)
            {
                EntityTempManager.getTemperatureCap(living).ifPresent(cap ->
                {   cap.deserializeModifiers(message.modifiers);
                });
            }
        });

        context.setPacketHandled(true);
    }
}