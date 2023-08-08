package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncForgeDataMessage
{
    CompoundTag forgeData;
    int entityID;
    String dimension;

    public SyncForgeDataMessage(Entity entity)
    {
        this.forgeData = entity.getPersistentData();
        this.entityID = entity.getId();
        this.dimension = entity.level.dimension().location().toString();
    }

    public SyncForgeDataMessage(CompoundTag forgeData, int entityID, String dimension)
    {
        this.forgeData = forgeData;
        this.entityID = entityID;
        this.dimension = dimension;
    }

    public static void encode(SyncForgeDataMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeNbt(message.forgeData);
        buffer.writeInt(message.entityID);
        buffer.writeUtf(message.dimension);
    }

    public static SyncForgeDataMessage decode(FriendlyByteBuf buffer)
    {
        return new SyncForgeDataMessage(buffer.readNbt(), buffer.readInt(), buffer.readUtf());
    }

    public static void handle(SyncForgeDataMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient() && ClientOnlyHelper.getClientLevel().dimension().location().toString().equals(message.dimension))
            {
                Level level = ClientOnlyHelper.getClientLevel();
                if (level != null)
                {
                    Entity entity = level.getEntity(message.entityID);
                    if (entity != null)
                    {   entity.getPersistentData().merge(message.forgeData);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
