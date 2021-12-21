package net.momostudios.coldsweat.core.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.momostudios.coldsweat.ColdSweat;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.message.*;

import java.util.ArrayList;
import java.util.List;

public class ColdSweatPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ColdSweat.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        INSTANCE.registerMessage(0, PlayerTempSyncMessage.class, PlayerTempSyncMessage::encode, PlayerTempSyncMessage::decode, PlayerTempSyncMessage::handle);
        INSTANCE.registerMessage(1, PlayerModifiersSyncMessage.class, PlayerModifiersSyncMessage::encode, PlayerModifiersSyncMessage::decode, PlayerModifiersSyncMessage::handle);
        INSTANCE.registerMessage(2, SoulLampInputMessage.class, SoulLampInputMessage::encode, SoulLampInputMessage::decode, SoulLampInputMessage::handle);
        INSTANCE.registerMessage(3, SoulLampInputClientMessage.class, SoulLampInputClientMessage::encode, SoulLampInputClientMessage::decode, SoulLampInputClientMessage::handle);
        INSTANCE.registerMessage(4, ClientConfigSendMessage.class, ClientConfigSendMessage::encode, ClientConfigSendMessage::decode, ClientConfigSendMessage::handle);
        INSTANCE.registerMessage(5, ClientConfigAskMessage.class, ClientConfigAskMessage::encode, ClientConfigAskMessage::decode, ClientConfigAskMessage::handle);
        INSTANCE.registerMessage(6, ClientConfigRecieveMessage.class, ClientConfigRecieveMessage::encode, ClientConfigRecieveMessage::decode, ClientConfigRecieveMessage::handle);
        INSTANCE.registerMessage(7, PlayerDamageMessage.class, PlayerDamageMessage::encode, PlayerDamageMessage::decode, PlayerDamageMessage::handle);
        INSTANCE.registerMessage(8, GracePeriodAskMessage.class, GracePeriodAskMessage::encode, GracePeriodAskMessage::decode, GracePeriodAskMessage::handle);
    }
    
    public static void writeConfigCacheToBuffer(ConfigCache config, PacketBuffer buffer)
    {
        buffer.writeInt(config.difficulty);
        buffer.writeDouble(config.minTemp);
        buffer.writeDouble(config.maxTemp);
        buffer.writeDouble(config.rate);
        buffer.writeBoolean(config.fireRes);
        buffer.writeBoolean(config.iceRes);
        buffer.writeBoolean(config.damageScaling);
        buffer.writeBoolean(config.showAmbient);
        buffer.writeInt(config.gracePeriodLength);
        buffer.writeBoolean(config.gracePeriodEnabled);
    }

    public static ConfigCache readConfigCacheFromBuffer(PacketBuffer buffer)
    {
        ConfigCache config = new ConfigCache();
        config.difficulty = buffer.readInt();
        config.minTemp = buffer.readDouble();
        config.maxTemp = buffer.readDouble();
        config.rate = buffer.readDouble();
        config.fireRes = buffer.readBoolean();
        config.iceRes = buffer.readBoolean();
        config.damageScaling = buffer.readBoolean();
        config.showAmbient = buffer.readBoolean();
        config.gracePeriodLength = buffer.readInt();
        config.gracePeriodEnabled = buffer.readBoolean();
        return config;
    }

    public static CompoundNBT writeListOfLists(List<? extends List<String>> list)
    {
        CompoundNBT tag = new CompoundNBT();
        for (int i = 0; i < list.size(); i++)
        {
            List<String> sublist = list.get(i);
            ListNBT subtag = new ListNBT();
            for (int j = 0; j < sublist.size(); j++)
            {
                subtag.add(StringNBT.valueOf(sublist.get(j)));
            }
            tag.put("" + i, subtag);
        }
        return tag;
    }

    public static List<? extends List<String>> getListOfLists(CompoundNBT tag)
    {
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++)
        {
            ListNBT subtag = tag.getList("" + i, 8);
            List<String> sublist = new ArrayList<>();
            for (int j = 0; j < subtag.size(); j++)
            {
                sublist.add(subtag.getString(j));
            }
            list.add(sublist);
        }
        return list;
    }
}
