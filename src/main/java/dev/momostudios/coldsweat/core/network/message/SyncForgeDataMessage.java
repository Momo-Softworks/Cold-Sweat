package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncForgeDataMessage
{
    CompoundNBT forgeData;
    int entityID;
    String dimension;

    public SyncForgeDataMessage(Entity entity)
    {
        this.forgeData = entity.getPersistentData();
        this.entityID = entity.getId();
        this.dimension = entity.level.dimension().location().toString();
    }

    public SyncForgeDataMessage(CompoundNBT forgeData, int entityID, String dimension)
    {
        this.forgeData = forgeData;
        this.entityID = entityID;
        this.dimension = dimension;
    }

    public static void encode(SyncForgeDataMessage message, PacketBuffer buffer)
    {
        buffer.writeNbt(message.forgeData);
        buffer.writeInt(message.entityID);
        buffer.writeUtf(message.dimension);
    }

    public static SyncForgeDataMessage decode(PacketBuffer buffer)
    {
        return new SyncForgeDataMessage(buffer.readNbt(), buffer.readInt(), buffer.readUtf());
    }

    public static void handle(SyncForgeDataMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient() && ClientOnlyHelper.getClientWorld().dimension().location().toString().equals(message.dimension))
            {
                World world = ClientOnlyHelper.getClientWorld();
                if (world != null)
                {
                    Entity entity = world.getEntity(message.entityID);
                    if (entity != null)
                    {   entity.getPersistentData().merge(message.forgeData);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
