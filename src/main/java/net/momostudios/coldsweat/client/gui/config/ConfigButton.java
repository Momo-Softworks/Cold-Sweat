package net.momostudios.coldsweat.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;

public class ConfigButton extends Button
{
    ConfigCache configCache;

    public ConfigButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction, ConfigCache cache)
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
            configCache.difficulty = 4;

        if (Minecraft.getInstance().currentScreen instanceof ConfigScreen.PageOne)
        {
            ((ConfigScreen.PageOne) Minecraft.getInstance().currentScreen).difficultyButton.setMessage(
                    new StringTextComponent(new TranslationTextComponent("cold_sweat.config.difficulty.name").getString() +
                    " (" + ConfigScreen.difficultyName(configCache.difficulty) + ")..."));
        }

        super.onPress();
    }
}
