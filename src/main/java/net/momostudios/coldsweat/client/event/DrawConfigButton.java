package net.momostudios.coldsweat.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.client.gui.config.ConfigScreen;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DrawConfigButton
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void eventHandler(GuiScreenEvent.InitGuiEvent event)
    {
        if (event.getGui() instanceof OptionsScreen)
        {
            event.addWidget(
                new ImageButton(event.getGui().width / 2 - 183, event.getGui().height / 6 + 120 - 10, 24, 24, 0, 40, 24,
                    new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button ->
                    Minecraft.getInstance().displayGuiScreen(new ConfigScreen.PageOne())));

        }
    }
}
