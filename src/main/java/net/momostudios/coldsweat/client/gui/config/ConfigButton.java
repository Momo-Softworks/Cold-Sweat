package net.momostudios.coldsweat.client.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigButton extends Button
{
    public ConfigButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
        super(x, y, width, height, title, pressedAction);
    }

    public boolean setsCustomDifficulty() {
        return true;
    }

    @Override
    public void onPress()
    {
        if (setsCustomDifficulty())
            ConfigScreen.INSTANCE.difficulty = 4;

        if (Minecraft.getInstance().currentScreen instanceof ConfigScreen.PageOne)
        {
            ((ConfigScreen.PageOne) Minecraft.getInstance().currentScreen).difficultyButton.setMessage(
                    new StringTextComponent(new TranslationTextComponent("cold_sweat.config.difficulty.name").getString() +
                    " (" + ConfigScreen.INSTANCE.difficultyName() + ")..."));
        }

        super.onPress();
    }
}
