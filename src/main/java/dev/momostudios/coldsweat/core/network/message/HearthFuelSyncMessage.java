package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HearthFuelSyncMessage
{
    BlockPos hearthPos;
    int hotFuel;
    int coldFuel;

    public HearthFuelSyncMessage() {
    }

    public HearthFuelSyncMessage(BlockPos hearthPos, int hotFuel, int coldFuel) {
        this.hearthPos = hearthPos;
        this.hotFuel = hotFuel;
        this.coldFuel = coldFuel;
    }

    public static void encode(HearthFuelSyncMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.hearthPos);
        buffer.writeInt(message.hotFuel);
        buffer.writeInt(message.coldFuel);
    }

    public static HearthFuelSyncMessage decode(FriendlyByteBuf buffer)
    {
        return new HearthFuelSyncMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(HearthFuelSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            BlockEntity te = Minecraft.getInstance().level.getBlockEntity(message.hearthPos);
            if (te instanceof HearthBlockEntity hearth)
            {
                hearth.setHotFuel(message.hotFuel);
                hearth.setColdFuel(message.coldFuel);
            }
        });
        context.setPacketHandled(true);
    }
}
