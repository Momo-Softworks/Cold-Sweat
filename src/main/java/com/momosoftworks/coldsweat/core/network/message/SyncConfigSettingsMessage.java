package com.momosoftworks.coldsweat.core.network.message;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

/**
 * Sent from the server to a joining client to push all synced config settings
 * (difficulty, temperature thresholds, fuel/insulation maps, etc.) so the client
 * displays correct values. Uses {@link ConfigSettings#encode()} / {@link ConfigSettings#decode(String, NBTTagCompound)}.
 */
public class SyncConfigSettingsMessage implements IMessage
{
    NBTTagCompound configData = new NBTTagCompound();

    public SyncConfigSettingsMessage()
    {   // no-arg constructor
    }

    public SyncConfigSettingsMessage(NBTTagCompound configData)
    {   this.configData = configData;
    }

    /**
     * Bundles all synced config settings into a single tag for transmission.
     */
    public static SyncConfigSettingsMessage create()
    {
        NBTTagCompound tag = new NBTTagCompound();
        Map<String, NBTTagCompound> encoded = ConfigSettings.encode();
        for (Map.Entry<String, NBTTagCompound> entry : encoded.entrySet())
        {   tag.setTag(entry.getKey(), entry.getValue());
        }
        return new SyncConfigSettingsMessage(tag);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {   ColdSweatPacketHandler.writeCompoundNBTToBuffer(buf, configData);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {   this.configData = ColdSweatPacketHandler.readCompoundNBTFromBuffer(buf);
    }

    public static class Handler implements IMessageHandler<SyncConfigSettingsMessage, IMessage>
    {
        @Override
        public IMessage onMessage(SyncConfigSettingsMessage message, MessageContext ctx)
        {
            if (message.configData == null) return null;
            for (Object keyObj : message.configData.func_150296_c())
            {   String key = (String) keyObj;
                ConfigSettings.decode(key, message.configData.getCompoundTag(key));
            }
            return null;
        }
    }
}
