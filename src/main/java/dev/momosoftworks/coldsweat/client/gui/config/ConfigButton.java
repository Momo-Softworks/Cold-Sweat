package dev.momosoftworks.coldsweat.client.gui.config;

import dev.momosoftworks.coldsweat.client.gui.config.pages.ConfigPageDifficulty;
import dev.momosoftworks.coldsweat.client.gui.config.pages.ConfigPageOne;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import dev.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.network.chat.Component;

public class ConfigButton extends Button
{
    public ConfigButton(int x, int y, int width, int height, Component title, Button.OnPress pressedAction)
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
                        Component.literal(Component.translatable("cold_sweat.config.difficulty.name").getString() +
                                " (" + ConfigPageDifficulty.getDifficultyName(ConfigSettings.DIFFICULTY.get()) + ")..."));
            }
        }

        super.onPress();
    }
}
