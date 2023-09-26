package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HearthResetMessage
{
    BlockPos blockPos;

    public HearthResetMessage(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public static void encode(HearthResetMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.blockPos);
    }

    public static HearthResetMessage decode(FriendlyByteBuf buffer)
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
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(message.blockPos);
                if (te instanceof HearthBlockEntity hearth)
                {   hearth.forceUpdate();
                }
            });
        }
        context.setPacketHandled(true);
    }
}
