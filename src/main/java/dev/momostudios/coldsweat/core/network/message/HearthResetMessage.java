package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.tileentity.HearthTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class HearthResetMessage
{
    BlockPos blockPos;
    public HearthResetMessage() {
    }

    public HearthResetMessage(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void encode(HearthResetMessage message, PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.blockPos);
    }

    public static HearthResetMessage decode(PacketBuffer buffer)
    {
        return new HearthResetMessage(buffer.readBlockPos());
    }

    public static void handle(HearthResetMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                TileEntity te = Minecraft.getInstance().level.getBlockEntity(message.blockPos);
                if (te instanceof HearthTileEntity)
                {
                    ((HearthTileEntity) te).forceUpdate();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
