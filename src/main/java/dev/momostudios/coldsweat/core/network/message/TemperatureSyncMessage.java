package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.Overlays;
import dev.momostudios.coldsweat.common.capability.PlayerTempCap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TemperatureSyncMessage
{
    int entityId;
    CompoundNBT temps;
    boolean instant;

    public TemperatureSyncMessage(LivingEntity entity, CompoundNBT temps, boolean instant)
    {   this.entityId = entity.getId();
        this.temps = temps;
        this.instant = instant;
    }

    TemperatureSyncMessage(int entityId, CompoundNBT temps, boolean instant)
    {   this.entityId = entityId;
        this.temps = temps;
        this.instant = instant;
    }

    public static void encode(TemperatureSyncMessage message, PacketBuffer buffer)
    {   buffer.writeInt(message.entityId);
        buffer.writeNbt(message.temps);
        buffer.writeBoolean(message.instant);
    }

    public static TemperatureSyncMessage decode(PacketBuffer buffer)
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
                    Temperature.getTemperatureCap(entity).ifPresent(cap ->
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
