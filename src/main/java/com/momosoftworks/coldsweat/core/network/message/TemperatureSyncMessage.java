package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.capability.temperature.PlayerTempCap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TemperatureSyncMessage implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<TemperatureSyncMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_temperature"));
    public static final StreamCodec<FriendlyByteBuf, TemperatureSyncMessage> CODEC = CustomPacketPayload.codec(TemperatureSyncMessage::encode, TemperatureSyncMessage::decode);

    int entityId;
    CompoundTag traits;
    boolean instant;

    public TemperatureSyncMessage(LivingEntity entity, CompoundTag traits, boolean instant)
    {   this.entityId = entity.getId();
        this.traits = traits;
        this.instant = instant;
    }

    TemperatureSyncMessage(int entityId, CompoundTag traits, boolean instant)
    {   this.entityId = entityId;
        this.traits = traits;
        this.instant = instant;
    }

    public void encode(FriendlyByteBuf buffer)
    {   buffer.writeInt(this.entityId);
        buffer.writeNbt(this.traits);
        buffer.writeBoolean(this.instant);
    }

    public static TemperatureSyncMessage decode(FriendlyByteBuf buffer)
    {   return new TemperatureSyncMessage(buffer.readInt(), buffer.readNbt(), buffer.readBoolean());
    }

    public static void handle(TemperatureSyncMessage message, IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            LivingEntity entity = (LivingEntity) Minecraft.getInstance().level.getEntity(message.entityId);

            if (entity != null)
            {
                ITemperatureCap cap = EntityTempManager.getTemperatureCap(entity);
                cap.deserializeTraits(message.traits);
                if (message.instant && cap instanceof PlayerTempCap)
                {   Overlays.setBodyTempInstant(cap.getTrait(Temperature.Trait.BODY));
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }
}
