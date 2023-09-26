package com.momosoftworks.coldsweat.client.event;

import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ClientSettingsConfig;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.core.network.message.ClientConfigAskMessage;
import com.momosoftworks.coldsweat.util.ClientOnlyHelper;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class DrawConfigButton
{
    public static boolean DRAW_CONTROLS = false;

    @SubscribeEvent
    public static void eventHandler(GuiScreenEvent.InitGuiEvent event)
    {
        if (event.getGui() instanceof OptionsScreen && ClientSettingsConfig.getInstance().isConfigButtonEnabled())
        {
            // The offset from the config
            Supplier<List<? extends Integer>> buttonPos = () -> ClientSettingsConfig.getInstance().getConfigButtonPos();
            AtomicInteger buttonX = new AtomicInteger(buttonPos.get().get(0));
            AtomicInteger buttonY = new AtomicInteger(buttonPos.get().get(1));
            int screenWidth = event.getGui().width;
            int screenHeight = event.getGui().height;

            if (buttonX.get() < -1 || buttonY.get() < -1)
            {   buttonX.set(0);
                buttonY.set(0);
                ClientSettingsConfig.getInstance().setConfigButtonPos(Arrays.asList(0, 0));
                ClientSettingsConfig.getInstance().save();
            }

            // Main config button
            ImageButton mainButton = new ImageButton(event.getGui().width / 2 - 183 + buttonX.get(), event.getGui().height / 6 + 110 + buttonY.get(),
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
            event.addWidget(mainButton);

            // Reconfigure the options screen with controls for the position of the config button
            if (DRAW_CONTROLS)
            {
                int buttonStartX = event.getGui().width / 2 - 183;
                int buttonStartY = event.getGui().height / 6 + 110;
                Runnable saveAndClamp = () ->
                {   ClientSettingsConfig.getInstance().setConfigButtonPos(Arrays.asList(buttonX.get(), buttonY.get()));
                    ClientSettingsConfig.getInstance().save();
                    buttonX.set(CSMath.clamp(buttonX.get(), -buttonStartX, screenWidth - mainButton.getWidth() - buttonStartX));
                    buttonY.set(CSMath.clamp(buttonY.get(), -buttonStartY, screenHeight - mainButton.getHeight() - buttonStartY));
                    mainButton.setPosition(buttonStartX + buttonX.get(), buttonStartY + buttonY.get());
                };

                AtomicReference<AbstractButton> doneButtonAtomic = new AtomicReference<>(null);
                // Disable all other buttons
                event.getGui().children().forEach(child ->
                {
                    // Don't disable "Done" button
                    if (child instanceof AbstractButton)
                    {
                        AbstractButton button = (AbstractButton) child;
                        boolean isDoneButton = button.getMessage().getString().equals(DialogTexts.GUI_DONE.getString());
                        if (!isDoneButton)
                            button.active = false;
                        else
                        {   doneButtonAtomic.set(button);
                            button.setWidth(button.getWidth() - 72);
                        }
                    }
                    if (child instanceof Slider)
                    {   ((Slider) child).active = false;
                    }
                });

                if (doneButtonAtomic.get() == null) return;
                AbstractButton doneButton = doneButtonAtomic.get();

                // Create "left" button
                ImageButton leftButton = new ImageButton(doneButton.x + doneButton.getWidth() + 2, doneButton.y, 14, 20, 0, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   buttonX.set(buttonX.get() - ConfigScreen.SHIFT_AMOUNT.get());
                                         saveAndClamp.run();
                                     });
                // Add left button
                event.addWidget(leftButton);

                // Create "up" button
                ImageButton upButton = new ImageButton(leftButton.x + leftButton.getWidth(), leftButton.y, 20, 10, 14, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   buttonY.set(buttonY.get() - ConfigScreen.SHIFT_AMOUNT.get());
                                         saveAndClamp.run();
                                     });
                // Add up button
                event.addWidget(upButton);

                // Create "down" button
                ImageButton downButton = new ImageButton(upButton.x, upButton.y + upButton.getHeight(), 20, 10, 14, 10, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   buttonY.set(buttonY.get() + ConfigScreen.SHIFT_AMOUNT.get());
                                         saveAndClamp.run();
                                     });
                // Add down button
                event.addWidget(downButton);

                // Create "right" button
                ImageButton rightButton = new ImageButton(upButton.x + upButton.getWidth(), upButton.y, 14, 20, 34, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   buttonX.set(buttonX.get() + ConfigScreen.SHIFT_AMOUNT.get());
                                         saveAndClamp.run();
                                     });
                // Add right button
                event.addWidget(rightButton);

                // Create "reset" button
                ImageButton resetButton = new ImageButton(rightButton.x + rightButton.getWidth() + 2, rightButton.y, 20, 20, 48, 0, 20,
                                     new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png"),
                                     button ->
                                     {   buttonX.set(0);
                                         buttonY.set(0);
                                         saveAndClamp.run();
                                     });
                // Add reset button
                event.addWidget(resetButton);

                TaskScheduler.scheduleClient(() -> DRAW_CONTROLS = false, 1);
            }
        }
    }
}
