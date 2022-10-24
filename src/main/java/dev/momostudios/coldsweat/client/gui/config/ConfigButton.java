package dev.momostudios.coldsweat.client.gui.config;

import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import dev.momostudios.coldsweat.util.config.ConfigSettings;

public class ConfigButton extends Button
{
    ConfigSettings configSettings = ConfigSettings.getInstance();

    public ConfigButton(int x, int y, int width, int height, BaseComponent title, Button.OnPress pressedAction)
    {
        super(x, y, width, height, title, pressedAction);
    }

    public boolean setsCustomDifficulty() {
        return true;
    }

    @Override
    public void onPress()
    {
        if (setsCustomDifficulty())
        {
            configSettings.difficulty = 4;

            if (Minecraft.getInstance().screen instanceof ConfigPageOne page)
            {
                ((Button) page.getWidgetBatch("difficulty").get(0)).setMessage(
                        new TextComponent(new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                                " (" + ConfigScreen.difficultyName(configSettings.difficulty) + ")..."));
            }
        }

        super.onPress();
    }
}
