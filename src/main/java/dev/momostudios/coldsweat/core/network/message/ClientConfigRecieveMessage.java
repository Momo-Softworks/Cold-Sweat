package dev.momostudios.coldsweat.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class ClientConfigRecieveMessage
{
    ConfigCache configCache;
    boolean openMenu;

    public ClientConfigRecieveMessage(ConfigCache configCache, boolean openMenu)
    {
        this.configCache = configCache;
        this.openMenu = openMenu;
    }

    public static void encode(ClientConfigRecieveMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.openMenu);
        ColdSweatPacketHandler.writeConfigCacheToBuffer(message.configCache, buffer);
    }

    public static ClientConfigRecieveMessage decode(FriendlyByteBuf buffer)
    {
        boolean onJoin = buffer.readBoolean();
        ConfigCache configCache = ColdSweatPacketHandler.readConfigCacheFromBuffer(buffer);

        return new ClientConfigRecieveMessage(configCache, onJoin);
    }

    public static void handle(ClientConfigRecieveMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                ConfigCache.setInstance(message.configCache);
                if (message.openMenu)
                {
                    try
                    {
                        LocalPlayer localPlayer = Minecraft.getInstance().player;
                        if (localPlayer != null)
                        {
                            Constructor configScreen = Class.forName("dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne").getConstructor(Class.forName("net.minecraft.client.gui.screens.Screen"), ConfigCache.class);
                            Minecraft.getInstance().setScreen((Screen) configScreen.newInstance(Minecraft.getInstance().screen, message.configCache));
                        }
                    }
                    catch (Exception ignored) {}
                }
            }
        });
        context.setPacketHandled(true);
    }
}
