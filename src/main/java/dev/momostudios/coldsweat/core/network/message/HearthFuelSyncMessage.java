package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import dev.momostudios.coldsweat.common.te.HearthTileEntity;

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

    public static void encode(HearthFuelSyncMessage message, PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.hearthPos);
        buffer.writeInt(message.hotFuel);
        buffer.writeInt(message.coldFuel);
    }

    public static HearthFuelSyncMessage decode(PacketBuffer buffer)
    {
        return new HearthFuelSyncMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(HearthFuelSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            TileEntity te = Minecraft.getInstance().world.getTileEntity(message.hearthPos);
            if (te instanceof HearthTileEntity)
            {
                HearthTileEntity hearth = (HearthTileEntity) te;
                hearth.setHotFuel(message.hotFuel);
                hearth.setColdFuel(message.coldFuel);
            }
        });
        context.setPacketHandled(true);
    }
}
