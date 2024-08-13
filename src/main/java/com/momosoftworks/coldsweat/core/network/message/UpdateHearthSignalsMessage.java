package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateHearthSignalsMessage
{
    private final boolean isSidePowered;
    private final boolean isBackPowered;
    private final BlockPos pos;

    public UpdateHearthSignalsMessage(boolean isSidePowered, boolean isBackPowered, BlockPos pos)
    {
        this.isSidePowered = isSidePowered;
        this.isBackPowered = isBackPowered;
        this.pos = pos;
    }

    public static void encode(UpdateHearthSignalsMessage message, PacketBuffer buf)
    {
        buf.writeBoolean(message.isSidePowered);
        buf.writeBoolean(message.isBackPowered);
        buf.writeBlockPos(message.pos);
    }

    public static UpdateHearthSignalsMessage decode(PacketBuffer buf)
    {
        return new UpdateHearthSignalsMessage(buf.readBoolean(), buf.readBoolean(), buf.readBlockPos());
    }

    public static void handle(UpdateHearthSignalsMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                TileEntity te = Minecraft.getInstance().level.getBlockEntity(message.pos);
                if (te instanceof HearthBlockEntity)
                {
                    HearthBlockEntity hearth = (HearthBlockEntity) te;
                    hearth.setSidePowered(message.isSidePowered);
                    hearth.setBackPowered(message.isBackPowered);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
