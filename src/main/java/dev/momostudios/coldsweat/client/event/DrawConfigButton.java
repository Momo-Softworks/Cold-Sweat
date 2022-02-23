package dev.momostudios.coldsweat.client.event;

import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DrawConfigButton
{
    @SubscribeEvent
    public static void eventHandler(GuiScreenEvent.InitGuiEvent event)
    {
        if (event.getGui() instanceof OptionsScreen && ColdSweatConfig.getInstance().isButtonShowing())
        {
            event.addWidget(
                new ImageButton(event.getGui().width / 2 - 183, event.getGui().height / 6 + 120 - 10, 24, 24, 0, 40, 24,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"),
                button ->
                {
                    if (!Minecraft.getInstance().isSingleplayer() && Minecraft.getInstance().player != null)
                        ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage(false));
                    else
                    {
                        Minecraft.getInstance().displayGuiScreen(new ConfigScreen.PageOne(Minecraft.getInstance().currentScreen,
                                new ConfigCache(ColdSweatConfig.getInstance())));
                    }
                }));
        }
    }
}
