package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlockDataUpdateMessage
{
    BlockPos blockPos;
    CompoundTag tag;

    public BlockDataUpdateMessage(BlockPos blockPos, CompoundTag tag) {
        this.blockPos = blockPos;
        this.tag = tag;
    }

    public static void encode(BlockDataUpdateMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeNbt(message.tag);
    }

    public static BlockDataUpdateMessage decode(FriendlyByteBuf buffer)
    {
        return new BlockDataUpdateMessage(buffer.readBlockPos(), buffer.readNbt());
    }

    public static void handle(BlockDataUpdateMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                Minecraft minecraft = Minecraft.getInstance();
                BlockPos pos = message.blockPos;
                BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
                if (blockEntity != null)
                {
                    blockEntity.getTileData().merge(message.tag);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
