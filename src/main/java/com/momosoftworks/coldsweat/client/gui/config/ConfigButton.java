package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.client.gui.config.pages.ConfigPageOne;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfigButton extends Button.Plain
{
    public ConfigButton(int x, int y, int width, int height, Component title, OnPress pressedAction)
    {   super(x, y, width, height, title, pressedAction, (button) -> MutableComponent.create(title.getContents()));
    }

    public boolean setsCustomDifficulty() {
        return true;
    }

    @Override
    public void onPress(InputWithModifiers input)
    {
        if (setsCustomDifficulty())
        {
            ConfigSettings.DIFFICULTY.set(ConfigSettings.Difficulty.CUSTOM);

            if (Minecraft.getInstance().gui.screen() instanceof ConfigPageOne page)
            {
                ((Button) page.getWidgetBatch("difficulty").get(0)).setMessage(
                        Component.literal(Component.translatable("cold_sweat.config.difficulty.name").getString() +
                                " (" + ConfigSettings.Difficulty.getFormattedName(ConfigSettings.DIFFICULTY.get()).getString() + ")..."));
            }
        }

        super.onPress(input);
    }
}
