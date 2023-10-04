package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.common.event.EntityTempManager;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class SyncTemperaturesMessage implements IMessage
{
    int entityId = 0;
    NBTTagCompound temps = new NBTTagCompound();
    boolean instant = false;

    public SyncTemperaturesMessage()
    {   // no-arg constructor
    }

    public SyncTemperaturesMessage(int entityId, NBTTagCompound temps, boolean instant)
    {   this.entityId = entityId;
        this.temps = temps;
        this.instant = instant;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {   buf.writeInt(entityId);
        buf.writeByte(instant ? 1 : 0);
        ColdSweatPacketHandler.writeCompoundNBTToBuffer(buf, temps);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {   this.entityId = buf.readInt();
        this.instant = buf.readByte() == 1;
        this.temps = ColdSweatPacketHandler.readCompoundNBTFromBuffer(buf);
    }

    public static class Handler implements IMessageHandler<SyncTemperaturesMessage, IMessage>
    {
        @Override
        public IMessage onMessage(SyncTemperaturesMessage message, MessageContext ctx)
        {   EntityPlayer thePlayer = (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity);
            Entity entity = thePlayer.worldObj.getEntityByID(message.entityId);

            EntityTempManager.getTemperatureProperty(entity).deserializeTemps(message.temps);
            return null; // no response in this case
        }
    }
}
