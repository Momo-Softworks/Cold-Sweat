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

public class SyncModifiersMessage implements IMessage
{
    int entityId = 0;
    NBTTagCompound modifiers = new NBTTagCompound();

    public SyncModifiersMessage()
    {   // no-arg constructor
    }

    public SyncModifiersMessage(int entityId, NBTTagCompound mods)
    {   this.entityId = entityId;
        this.modifiers = mods;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {   buf.writeInt(this.entityId);
        ColdSweatPacketHandler.writeCompoundNBTToBuffer(buf, this.modifiers);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {   this.entityId = buf.readInt();
        this.modifiers = ColdSweatPacketHandler.readCompoundNBTFromBuffer(buf);
    }

    public static class Handler implements IMessageHandler<SyncModifiersMessage, IMessage>
    {
        @Override
        public IMessage onMessage(SyncModifiersMessage message, MessageContext ctx)
        {   EntityPlayer thePlayer = (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity);
            Entity entity = thePlayer.worldObj.getEntityByID(message.entityId);

            EntityTempManager.getTemperatureProperty(entity).deserializeModifiers(message.modifiers);
            return null; // no response in this case
        }
    }
}
