package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class HearthResetMessage
{
    BlockPos blockPos;
    Collection<BlockPos> updates;
    public HearthResetMessage() {
    }

    public HearthResetMessage(BlockPos blockPos, Collection<BlockPos> updates) {
        this.blockPos = blockPos;
        this.updates = updates;
    }

    public static void encode(HearthResetMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeCollection(message.updates, FriendlyByteBuf::writeBlockPos);
    }

    public static HearthResetMessage decode(FriendlyByteBuf buffer)
    {
        return new HearthResetMessage(buffer.readBlockPos(), buffer.readCollection(ArrayList::new, FriendlyByteBuf::readBlockPos));
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
                {
                    for (BlockPos pos : message.updates)
                    {
                        hearth.forceUpdate(pos);
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
