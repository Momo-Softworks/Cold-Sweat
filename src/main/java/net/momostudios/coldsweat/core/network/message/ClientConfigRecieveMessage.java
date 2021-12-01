package net.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.coldsweat.config.ConfigCache;
import net.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class ClientConfigRecieveMessage
{
    ConfigCache configCache;
    boolean onJoin;

    public ClientConfigRecieveMessage(ConfigCache configCache, boolean onJoin) {
        this.configCache = configCache;
        this.onJoin = onJoin;
    }

    public static void encode(ClientConfigRecieveMessage message, PacketBuffer buffer)
    {
        buffer.writeBoolean(message.onJoin);
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
    }

    public static ClientConfigRecieveMessage decode(PacketBuffer buffer)
    {
        boolean onJoin = buffer.readBoolean();
        return new ClientConfigRecieveMessage(ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer), onJoin);
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
