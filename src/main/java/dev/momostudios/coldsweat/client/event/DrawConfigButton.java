package dev.momostudios.coldsweat.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.core.event.TaskScheduler;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ClientConfigAskMessage;
import dev.momostudios.coldsweat.util.ClientOnlyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DrawConfigButton
{
    public static boolean DRAW_CONTROLS = false;

    @SubscribeEvent
    public static void eventHandler(ScreenEvent.Init event)
    {
        if (event.getScreen() instanceof OptionsScreen && ClientSettingsConfig.getInstance().isButtonShowing())
        {
            // The offset from the config
            Supplier<List<? extends Integer>> buttonPos = () -> ClientSettingsConfig.getInstance().getConfigButtonPos();
            Supplier<Integer> buttonX = () -> buttonPos.get().get(0);
            Supplier<Integer> buttonY = () -> buttonPos.get().get(1);
            // The button's absolute position on screen
            Supplier<Integer> absButtonX = () -> event.getScreen().width / 2 - 183 + buttonX.get();
            Supplier<Integer> absButtonY = () -> event.getScreen().height / 6 + 110 + buttonY.get();

            // Main config button
            ImageButton mainButton = new ImageButton(event.getScreen().width / 2 - 183 + buttonX.get(), event.getScreen().height / 6 + 110 + buttonY.get(),
                                     24, 24, 0, 40, 24,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {
                                         if (!Minecraft.getInstance().isLocalServer() && Minecraft.getInstance().player != null)
                                         {   ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigAskMessage(Minecraft.getInstance().player.getUUID()));
                                         }
                                         else ClientOnlyHelper.openConfigScreen();
                                     });
            // Add main button
            event.addListener(mainButton);

            if (DRAW_CONTROLS)
            {
                // Disable all other buttons
                event.getScreen().children().forEach(child ->
                {
                    if (child instanceof Button button && !button.getMessage().getString().equals(CommonComponents.GUI_DONE.getString()))
                    {   button.active = false;
                    }
                });

                // Create "up" button
                ImageButton upButton = new ImageButton(mainButton.x + 2, mainButton.y - 12, 20, 10, 14, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   ClientSettingsConfig.getInstance().setConfigButtonPos(List.of(buttonX.get(), buttonY.get() - (Screen.hasShiftDown() ? 10 : 1)));
                                         ClientSettingsConfig.getInstance().save();
                                         mainButton.setPosition(absButtonX.get(), absButtonY.get());
                                     })
                {
                    @Override
                    public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTick)
                    {   super.renderButton(ps, mouseX, mouseY, partialTick);
                        this.setPosition(mainButton.x + 2, mainButton.y - 12);
                    }
                };
                // Add up button
                event.addListener(upButton);

                // Create "down" button
                ImageButton downButton = new ImageButton(mainButton.x + 2, mainButton.y + 26, 20, 10, 14, 10, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   ClientSettingsConfig.getInstance().setConfigButtonPos(List.of(buttonX.get(), buttonY.get() + (Screen.hasShiftDown() ? 10 : 1)));
                                         ClientSettingsConfig.getInstance().save();
                                         mainButton.setPosition(absButtonX.get(), absButtonY.get());
                                     })
                {
                    @Override
                    public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTick)
                    {   super.renderButton(ps, mouseX, mouseY, partialTick);
                        this.setPosition(mainButton.x + 2, mainButton.y + 26);
                    }
                };
                // Add down button
                event.addListener(downButton);

                // Create "left" button
                ImageButton leftButton = new ImageButton(mainButton.x - 16, mainButton.y + 2, 14, 20, 0, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   ClientSettingsConfig.getInstance().setConfigButtonPos(List.of(buttonX.get() - (Screen.hasShiftDown() ? 10 : 1), buttonY.get()));
                                         ClientSettingsConfig.getInstance().save();
                                         mainButton.setPosition(absButtonX.get(), absButtonY.get());
                                     })
                {
                    @Override
                    public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTick)
                    {   super.renderButton(ps, mouseX, mouseY, partialTick);
                        this.setPosition(mainButton.x - 16, mainButton.y + 2);
                    }
                };
                // Add left button
                event.addListener(leftButton);

                // Create "right" button
                ImageButton rightButton = new ImageButton(mainButton.x + 26, mainButton.y + 2, 14, 20, 34, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   ClientSettingsConfig.getInstance().setConfigButtonPos(List.of(buttonX.get() + (Screen.hasShiftDown() ? 10 : 1), buttonY.get()));
                                         ClientSettingsConfig.getInstance().save();
                                         mainButton.setPosition(absButtonX.get(), absButtonY.get());
                                     })
                {
                    @Override
                    public void renderButton(PoseStack ps, int mouseX, int mouseY, float partialTick)
                    {   super.renderButton(ps, mouseX, mouseY, partialTick);
                        this.setPosition(mainButton.x + 26, mainButton.y + 2);
                    }
                };
                // Add right button
                event.addListener(rightButton);

                TaskScheduler.scheduleClient(() -> DRAW_CONTROLS = false, 1);
            }
        }
    }
}
