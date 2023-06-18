package dev.momostudios.coldsweat.client.gui.config;

import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageDifficulty;
import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne;
import dev.momostudios.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigButton extends Button
{
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
            ConfigSettings.DIFFICULTY.set(4);

            if (Minecraft.getInstance().screen instanceof ConfigPageOne page)
            {
                ((Button) page.getWidgetBatch("difficulty").get(0)).setMessage(
                        new TextComponent(new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                                " (" + ConfigPageDifficulty.difficultyName(ConfigSettings.DIFFICULTY.get()) + ")..."));
            }
        }

        super.onPress();
    }
}
