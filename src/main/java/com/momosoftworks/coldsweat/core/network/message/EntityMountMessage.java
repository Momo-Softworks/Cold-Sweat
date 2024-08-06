package com.momosoftworks.coldsweat.core.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EntityMountMessage
{
    int entity;
    int vehicle;
    Action action;

    public EntityMountMessage(int entity, int vehicle, Action action)
    {
        this.entity = entity;
        this.vehicle = vehicle;
        this.action = action;
    }

    public static void encode(EntityMountMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.entity);
        buffer.writeInt(message.vehicle);
        buffer.writeEnum(message.action);
    }

    public static EntityMountMessage decode(PacketBuffer buffer)
    {   return new EntityMountMessage(buffer.readInt(), buffer.readInt(), buffer.readEnum(Action.class));
    }

    public static void handle(EntityMountMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT)
        {
            context.enqueueWork(() ->
            {
                Minecraft mc = Minecraft.getInstance();
                Entity entity = mc.level.getEntity(message.entity);
                Entity vehicle = mc.level.getEntity(message.vehicle);

                if (message.action == Action.MOUNT)
                {   entity.startRiding(vehicle, true);
                }
                else if (entity.getVehicle().getId() == message.vehicle)
                {   entity.stopRiding();
                }
            });
        }
        context.setPacketHandled(true);
    }

    public enum Action
    {
        MOUNT,
        DISMOUNT
    }
}
