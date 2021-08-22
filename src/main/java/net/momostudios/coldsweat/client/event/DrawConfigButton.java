package net.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.client.gui.ConfigScreen;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DrawConfigButton
{
    //private ImageButton customButton;

    //@OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void eventHandler(GuiScreenEvent.InitGuiEvent event)
    {
        if (event.getGui() instanceof OptionsScreen)
        {
            event.addWidget(
                new ImageButton(event.getGui().width / 2 - 183, event.getGui().height / 6 + 120 - 10, 24, 24, 0, 0, 24,
                    new ResourceLocation("cold_sweat:textures/gui/screen/configs/main_button.png"), button ->
                    Minecraft.getInstance().displayGuiScreen(new ConfigScreen(event.getGui()))));

        }
    }
}
