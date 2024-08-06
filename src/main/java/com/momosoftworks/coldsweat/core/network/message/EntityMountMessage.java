package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EntityMountMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<EntityMountMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "entity_mount"));
    public static final StreamCodec<FriendlyByteBuf, EntityMountMessage> CODEC = CustomPacketPayload.codec(EntityMountMessage::encode, EntityMountMessage::decode);

    int entity;
    int vehicle;
    Action action;

    public EntityMountMessage(int entity, int vehicle, Action action)
    {
        this.entity = entity;
        this.vehicle = vehicle;
        this.action = action;
    }

    public static void encode(EntityMountMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entity);
        buffer.writeInt(message.vehicle);
        buffer.writeEnum(message.action);
    }

    public static EntityMountMessage decode(FriendlyByteBuf buffer)
    {   return new EntityMountMessage(buffer.readInt(), buffer.readInt(), buffer.readEnum(Action.class));
    }

    public static void handle(EntityMountMessage message, IPayloadContext context)
    {
        if (context.flow().isClientbound())
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }

    public enum Action
    {
        MOUNT,
        DISMOUNT
    }
}
