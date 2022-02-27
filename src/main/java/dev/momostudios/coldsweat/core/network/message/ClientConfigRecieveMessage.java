package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class ClientConfigRecieveMessage
{
    ConfigCache configCache;
    boolean onJoin;

    public ClientConfigRecieveMessage(ConfigCache configCache, boolean onJoin)
    {
        this.configCache = configCache;
        this.onJoin = onJoin;
    }

    public static void encode(ClientConfigRecieveMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.onJoin);
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
        // WorldTempConfig
        buffer.writeNbt(ColdSweatPacketHandler.writeListOfLists(message.configCache.worldOptionsReference.get("biome_offsets")));
        buffer.writeNbt(ColdSweatPacketHandler.writeListOfLists(message.configCache.worldOptionsReference.get("dimension_offsets")));
        buffer.writeNbt(ColdSweatPacketHandler.writeListOfLists(message.configCache.worldOptionsReference.get("biome_temperatures")));
        buffer.writeNbt(ColdSweatPacketHandler.writeListOfLists(message.configCache.worldOptionsReference.get("dimension_temperatures")));
    }

    public static ClientConfigRecieveMessage decode(FriendlyByteBuf buffer)
    {
        boolean onJoin = buffer.readBoolean();
        ConfigCache configCache = ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer);
        configCache.worldOptionsReference.put("biome_offsets", ColdSweatPacketHandler.readListOfLists(buffer.readNbt()));
        configCache.worldOptionsReference.put("dimension_offsets", ColdSweatPacketHandler.readListOfLists(buffer.readNbt()));
        configCache.worldOptionsReference.put("biome_temperatures", ColdSweatPacketHandler.readListOfLists(buffer.readNbt()));
        configCache.worldOptionsReference.put("dimension_temperatures", ColdSweatPacketHandler.readListOfLists(buffer.readNbt()));

        return new ClientConfigRecieveMessage(configCache, onJoin);
    }

    public static void handle(ClientConfigRecieveMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (message.onJoin)
            {
                ConfigCache.setInstance(message.configCache);
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
                    Constructor constructor = ObfuscationReflectionHelper.findConstructor(Class.forName("dev.momostudios.coldsweat.client.gui.config.ConfigScreen$PageOne"),
                            Class.forName("net.minecraft.client.gui.screen.Screen"), ConfigCache.class);
                    displayScreen.invoke(Minecraft.getInstance(), constructor.newInstance(Minecraft.getInstance().screen, message.configCache));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
