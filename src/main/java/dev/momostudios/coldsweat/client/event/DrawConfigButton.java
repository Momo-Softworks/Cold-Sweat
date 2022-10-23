package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DrawConfigButton
{
    @SubscribeEvent
    public static void eventHandler(ScreenEvent.InitScreenEvent event)
    {
        if (event.getScreen() instanceof OptionsScreen && ColdSweatConfig.getInstance().isButtonShowing())
        {
            event.addListener
            (
                new ImageButton(event.getScreen().width / 2 - 183, event.getScreen().height / 6 + 120 - 10, 24, 24, 0, 40, 24,
                new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                button ->
                {
                    if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().player != null)
                    {
                        ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage(true));
                    }
                    else
                    {
                        Minecraft.getInstance().setScreen(new ConfigPageOne(Minecraft.getInstance().screen,
                                new ConfigSettings(ColdSweatConfig.getInstance())));
                    }
                })
            );
        }
    }
}
