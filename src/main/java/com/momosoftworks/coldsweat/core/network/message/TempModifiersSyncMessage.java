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

public class TempModifiersSyncMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TempModifiersSyncMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_temp_modifiers"));
    public static final StreamCodec<FriendlyByteBuf, TempModifiersSyncMessage> CODEC = CustomPacketPayload.codec(TempModifiersSyncMessage::encode, TempModifiersSyncMessage::decode);

    int entityId;
    CompoundTag modifiers;

    public TempModifiersSyncMessage(LivingEntity entity, CompoundTag modifiers)
    {
        this.entityId = entity.getId();
        this.modifiers = modifiers;
    }

    TempModifiersSyncMessage(int entityId, CompoundTag modifiers)
    {
        this.entityId = entityId;
        this.modifiers = modifiers;
    }

    public static void encode(TempModifiersSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.modifiers);
    }

    public static TempModifiersSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new TempModifiersSyncMessage(buffer.readInt(), buffer.readNbt());
    }

    public static void handle(TempModifiersSyncMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);

            if (entity instanceof LivingEntity living)
            {
                EntityTempManager.getTemperatureCap(living).deserializeModifiers(message.modifiers);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}