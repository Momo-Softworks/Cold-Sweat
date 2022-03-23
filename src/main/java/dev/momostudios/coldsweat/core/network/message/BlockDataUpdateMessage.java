package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import dev.momostudios.coldsweat.common.blockentity.HearthBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlockDataUpdateMessage
{
    BlockPos blockPos;
    List<Tag> tagValues;
    List<String> blockTags;

    public BlockDataUpdateMessage() {
    }

    public BlockDataUpdateMessage(BlockPos blockPos, List<String> blockTags, List<Tag> tagValues) {
        this.blockPos = blockPos;
        this.blockTags = blockTags;
        this.tagValues = tagValues;
    }

    public static void encode(BlockDataUpdateMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBlockPos(message.blockPos);
        if (message.blockTags.size() == message.tagValues.size())
        {
            CompoundTag tags = new CompoundTag();
            for (int i = 0; i < message.blockTags.size(); i++)
            {
                tags.put(message.blockTags.get(i), message.tagValues.get(i));
            }
            buffer.writeNbt(tags);
        }
    }

    public static BlockDataUpdateMessage decode(FriendlyByteBuf buffer)
    {
        BlockPos blockPos = buffer.readBlockPos();

        CompoundTag tags = buffer.readNbt();
        List<String> blockTags = tags.getAllKeys().stream().toList();
        List<Tag> tagValues = new ArrayList<>(blockTags.size());
        for (String blockTag : blockTags)
        {
            tagValues.add(tags.get(blockTag));
        }

        return new BlockDataUpdateMessage(blockPos, blockTags, tagValues);
    }

    public static void handle(BlockDataUpdateMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                BlockEntity te = Minecraft.getInstance().level.getBlockEntity(message.blockPos);
                if (te != null)
                {
                    for (int i = 0; i < message.blockTags.size(); i++)
                    {
                        te.getTileData().put(message.blockTags.get(i), message.tagValues.get(i));
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }
}
