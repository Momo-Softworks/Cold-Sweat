package com.momosoftworks.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class BlockDataUpdateMessage
{
    BlockPos blockPos;
    TileEntity blockEntity;
    CompoundNBT tag;

    public BlockDataUpdateMessage(TileEntity blockEntity)
    {
        this.blockPos = blockEntity.getBlockPos();
        this.blockEntity = blockEntity;
    }

    public BlockDataUpdateMessage(BlockPos blockPos, CompoundNBT tag)
    {
        this.blockPos = blockPos;
        this.tag = tag;
    }

    public static void encode(BlockDataUpdateMessage message, PacketBuffer buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeNbt(message.blockEntity.getUpdateTag());
    }

    public static BlockDataUpdateMessage decode(PacketBuffer buffer)
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
                ClientWorld world = Minecraft.getInstance().level;
                if (world != null)
                {
                    TileEntity be = world.getBlockEntity(message.blockPos);
                    if (be != null)
                    {   be.handleUpdateTag(be.getBlockState(), message.tag);
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
