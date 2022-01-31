package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.common.te.HearthTileEntity;
import net.momostudios.coldsweat.common.temperature.modifier.TempModifier;
import net.momostudios.coldsweat.common.world.TempModifierEntries;
import net.momostudios.coldsweat.core.capabilities.PlayerTempCapability;
import net.momostudios.coldsweat.util.NBTHelper;
import net.momostudios.coldsweat.util.PlayerTemp;

import java.util.ArrayList;
import java.util.List;
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
