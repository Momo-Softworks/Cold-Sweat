package dev.momostudios.coldsweat.client.gui.config;

import dev.momostudios.coldsweat.client.gui.config.pages.ConfigPageOne;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import dev.momostudios.coldsweat.util.config.ConfigCache;

public class ConfigButton extends Button
{
    ConfigCache configCache = ConfigCache.getInstance();

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
            configCache.difficulty = 4;

            if (Minecraft.getInstance().screen instanceof ConfigPageOne page)
            {
                ((Button) page.getElementBatch("difficulty").get(0)).setMessage(
                        new TextComponent(new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                                " (" + ConfigScreen.difficultyName(configCache.difficulty) + ")..."));
            }
        }

        super.onPress();
    }
}
