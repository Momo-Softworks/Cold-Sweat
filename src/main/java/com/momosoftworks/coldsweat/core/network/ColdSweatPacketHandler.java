package com.momosoftworks.coldsweat.core.network;

import com.momosoftworks.coldsweat.core.network.message.SyncModifiersMessage;
import com.momosoftworks.coldsweat.core.network.message.SyncTemperaturesMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataInputStream;
import java.io.IOException;

public class ColdSweatPacketHandler
{
    public static final String NETWORK_ID = "ColdSweat";
    public static SimpleNetworkWrapper CHANNEL;

    public static void registerMessages()
    {
        ColdSweatPacketHandler.CHANNEL.registerMessage(SyncTemperaturesMessage.Handler.class, SyncTemperaturesMessage.class, 0, Side.CLIENT);
        ColdSweatPacketHandler.CHANNEL.registerMessage(SyncModifiersMessage.Handler.class, SyncModifiersMessage.class, 1, Side.CLIENT);
    }

    public static void writeCompoundNBTToBuffer(ByteBuf buf, NBTTagCompound nbt)
    {
        if (nbt == null)
        {   buf.writeByte(0);
        }
        else try
        {   CompressedStreamTools.write(nbt, new ByteBufOutputStream(buf));
        }
        catch (IOException ioexception)
        {   throw new EncoderException(ioexception);
        }
    }

    public static NBTTagCompound readCompoundNBTFromBuffer(ByteBuf buf)
    {
        int i = buf.readerIndex();
        byte b0 = buf.readByte();

        if (b0 == 0)
        {   return null;
        }
        else
        {   buf.readerIndex(i);
            try
            {   return CompressedStreamTools.read(new DataInputStream(new ByteBufInputStream(buf)));
            }
            catch (IOException ioexception)
            {   throw new EncoderException(ioexception);
            }
        }
    }
}
