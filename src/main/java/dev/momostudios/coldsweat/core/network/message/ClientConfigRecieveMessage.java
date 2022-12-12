package dev.momostudios.coldsweat.core.network.message;

import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public class ClientConfigRecieveMessage
{
    ConfigSettings configSettings;
    boolean openMenu;

    public ClientConfigRecieveMessage(ConfigSettings configSettings, boolean openMenu)
    {
        this.configSettings = configSettings;
        this.openMenu = openMenu;
    }

    public static void encode(ClientConfigRecieveMessage message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.openMenu);
        buffer.writeNbt(ConfigHelper.writeToNBT(message.configSettings));
    }

    public static ClientConfigRecieveMessage decode(FriendlyByteBuf buffer)
    {
        boolean onJoin = buffer.readBoolean();
        ConfigSettings configSettings = ConfigHelper.readFromNBT(buffer.readNbt());

        return new ClientConfigRecieveMessage(configSettings, onJoin);
    }

    public static void handle(ClientConfigRecieveMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
        {
            if (context.getDirection().getReceptionSide().isClient())
            {
                ConfigSettings.setInstance(message.configSettings);
                if (message.openMenu)
                {
                    try
                    {
                        LocalPlayer localPlayer = Minecraft.getInstance().player;
                        if (localPlayer != null)
                        {
                            Constructor configScreen = Class.forName("dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne").getConstructor(Class.forName("net.minecraft.client.gui.screens.Screen"), ConfigSettings.class);
                            Minecraft.getInstance().setScreen((Screen) configScreen.newInstance(Minecraft.getInstance().screen, message.configSettings));
                        }
                    }
                    catch (Exception ignored) {}
                }
            }
        });
        context.setPacketHandled(true);
    }
}
