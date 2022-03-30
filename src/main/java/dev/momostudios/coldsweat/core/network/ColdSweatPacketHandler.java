package dev.momostudios.coldsweat.core.network;

import dev.momostudios.coldsweat.core.network.message.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColdSweatPacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.1";
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
        INSTANCE.registerMessage(4, ClientConfigSendMessage.class, ClientConfigSendMessage::encode, ClientConfigSendMessage::decode, ClientConfigSendMessage::handle);
        INSTANCE.registerMessage(5, ClientConfigAskMessage.class, ClientConfigAskMessage::encode, ClientConfigAskMessage::decode, ClientConfigAskMessage::handle);
        INSTANCE.registerMessage(6, ClientConfigRecieveMessage.class, ClientConfigRecieveMessage::encode, ClientConfigRecieveMessage::decode, ClientConfigRecieveMessage::handle);
        INSTANCE.registerMessage(7, PlaySoundMessage.class, PlaySoundMessage::encode, PlaySoundMessage::decode, PlaySoundMessage::handle);
        INSTANCE.registerMessage(8, BlockDataUpdateMessage.class, BlockDataUpdateMessage::encode, BlockDataUpdateMessage::decode, BlockDataUpdateMessage::handle);
    }
    
    public static void writeConfigCacheToBuffer(ConfigCache config, FriendlyByteBuf buffer)
    {
        buffer.writeInt(config.difficulty);
        buffer.writeDouble(config.minTemp);
        buffer.writeDouble(config.maxTemp);
        buffer.writeDouble(config.rate);
        buffer.writeBoolean(config.fireRes);
        buffer.writeBoolean(config.iceRes);
        buffer.writeBoolean(config.damageScaling);
        buffer.writeBoolean(config.showWorldTemp);
        buffer.writeInt(config.graceLength);
        buffer.writeBoolean(config.graceEnabled);
    }

    public static ConfigCache readConfigCacheFromBuffer(FriendlyByteBuf buffer)
    {
        ConfigCache config = new ConfigCache();
        config.difficulty = buffer.readInt();
        config.minTemp = buffer.readDouble();
        config.maxTemp = buffer.readDouble();
        config.rate = buffer.readDouble();
        config.fireRes = buffer.readBoolean();
        config.iceRes = buffer.readBoolean();
        config.damageScaling = buffer.readBoolean();
        config.showWorldTemp = buffer.readBoolean();
        config.graceLength = buffer.readInt();
        config.graceEnabled = buffer.readBoolean();
        return config;
    }

    public static CompoundTag writeListOfLists(List<? extends List<?>> list)
    {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < list.size(); i++)
        {
            List<?> sublist = list.get(i);
            ListTag subtag = new ListTag();
            for (Object o : sublist)
            {
                subtag.add(StringTag.valueOf(o.toString()));
            }
            tag.put("" + i, subtag);
        }
        return tag;
    }

    public static List<List<Object>> readListOfLists(CompoundTag tag)
    {
        List<List<Object>> list = new ArrayList<>();
        for (int i = 0; i < tag.size(); i++)
        {
            ListTag subtag = tag.getList("" + i, 8);
            List<Object> sublist = IntStream.range(0, subtag.size()).mapToObj(j -> subtag.getString(j).transform(string ->
            {
                try
                {
                    return Double.parseDouble(string);
                }
                catch (Exception e)
                {
                    return string;
                }
            })).collect(Collectors.toList());
            list.add(sublist);
        }
        return list;
    }
}
