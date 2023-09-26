package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import com.momosoftworks.coldsweat.common.capability.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.PlayerTempCap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TemperatureSyncMessage
{
    int entityId;
    CompoundTag temps;
    boolean instant;

    public TemperatureSyncMessage(LivingEntity entity, CompoundTag temps, boolean instant)
    {   this.entityId = entity.getId();
        this.temps = temps;
        this.instant = instant;
    }

    TemperatureSyncMessage(int entityId, CompoundTag temps, boolean instant)
    {   this.entityId = entityId;
        this.temps = temps;
        this.instant = instant;
    }

    public static void encode(TemperatureSyncMessage message, FriendlyByteBuf buffer)
    {   buffer.writeInt(message.entityId);
        buffer.writeNbt(message.temps);
        buffer.writeBoolean(message.instant);
    }

    public static TemperatureSyncMessage decode(FriendlyByteBuf buffer)
    {   return new TemperatureSyncMessage(buffer.readInt(), buffer.readNbt(), buffer.readBoolean());
    }

    public static void handle(TemperatureSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();

        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                LivingEntity entity = (LivingEntity) Minecraft.getInstance().level.getEntity(message.entityId);

                if (entity != null)
                {
                    EntityTempManager.getTemperatureCap(entity).ifPresent(cap ->
                    {
                        cap.deserializeTemps(message.temps);
                        if (message.instant && cap instanceof PlayerTempCap)
                        {   Overlays.setBodyTemp(cap.getTemp(Temperature.Type.BODY));
                        }
                    });
                }
            });
        }

        context.setPacketHandled(true);
    }
}
