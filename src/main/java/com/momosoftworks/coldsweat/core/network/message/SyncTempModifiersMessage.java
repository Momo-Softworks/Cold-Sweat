package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncTempModifiersMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SyncTempModifiersMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_temp_modifiers"));
    public static final StreamCodec<FriendlyByteBuf, SyncTempModifiersMessage> CODEC = CustomPacketPayload.codec(SyncTempModifiersMessage::encode, SyncTempModifiersMessage::decode);

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

    public static void handle(SyncTempModifiersMessage message, IPayloadContext context)
    {
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
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}