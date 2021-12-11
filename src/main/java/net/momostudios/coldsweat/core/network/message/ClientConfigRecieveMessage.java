package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.config.WorldTemperatureConfig;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ClientConfigRecieveMessage
{
    ConfigCache configCache;
    boolean onJoin;
    Map<String, List<? extends List<String>>> worldTempConfig = new HashMap<>();

    public ClientConfigRecieveMessage(ConfigCache configCache, boolean onJoin, Map<String, List<? extends List<String>>> worldTempConfig)
    {
        this.configCache = configCache;
        this.onJoin = onJoin;
        if (onJoin)
        {
            this.worldTempConfig = worldTempConfig;
        }
    }

    public static void encode(ClientConfigRecieveMessage message, PacketBuffer buffer)
    {
        buffer.writeBoolean(message.onJoin);
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
        if (message.onJoin)
        {
            // WorldTempConfig
            message.worldTempConfig.forEach((key, value) ->
            {
                buffer.writeCompoundTag(ColdSweatPacketHandler.writeListOfLists(message.worldTempConfig.get(key)));
            });
        }
    }

    public static ClientConfigRecieveMessage decode(PacketBuffer buffer)
    {
        boolean onJoin = buffer.readBoolean();
        ConfigCache configCache = ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer);
        Map<String, List<? extends List<String>>> worldTempConfig = new HashMap<>();
        if (onJoin)
        {
            worldTempConfig.put("biomeOffsets", ColdSweatPacketHandler.getListOfLists(buffer.readCompoundTag()));
            worldTempConfig.put("dimensionOffsets", ColdSweatPacketHandler.getListOfLists(buffer.readCompoundTag()));
            worldTempConfig.put("biomeTemperatures", ColdSweatPacketHandler.getListOfLists(buffer.readCompoundTag()));
            worldTempConfig.put("dimensionTemperatures", ColdSweatPacketHandler.getListOfLists(buffer.readCompoundTag()));
        }

        return new ClientConfigRecieveMessage(configCache, onJoin, worldTempConfig);
    }

    public static void handle(ClientConfigRecieveMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (message.onJoin)
            {
                ConfigCache.setInstance(message.configCache);
                WorldTemperatureConfig.INSTANCE.setBiomeOffsets(message.worldTempConfig.get("biomeOffsets"));
                WorldTemperatureConfig.INSTANCE.setDimensionOffsets(message.worldTempConfig.get("dimensionOffsets"));
                WorldTemperatureConfig.INSTANCE.setBiomeTemperatures(message.worldTempConfig.get("biomeTemperatures"));
                WorldTemperatureConfig.INSTANCE.setDimensionTemperatures(message.worldTempConfig.get("dimensionTemperatures"));
            }
            else
            {
                try
                {
                    Method displayScreen;

                    try
                    {
                        displayScreen = ObfuscationReflectionHelper.findMethod(Minecraft.class, "displayGuiScreen", Class.forName("net.minecraft.client.gui.screen.Screen"));
                    } catch (ObfuscationReflectionHelper.UnableToFindMethodException e)
                    {
                        displayScreen = ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147108_a", Class.forName("net.minecraft.client.gui.screen.Screen"));
                    }
                    Constructor constructor = ObfuscationReflectionHelper.findConstructor(Class.forName("net.momostudios.coldsweat.client.gui.config.ConfigScreen$PageOne"),
                            Class.forName("net.minecraft.client.gui.screen.Screen"), ConfigCache.class);
                    displayScreen.invoke(Minecraft.getInstance(), constructor.newInstance(Minecraft.getInstance().currentScreen, message.configCache));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
