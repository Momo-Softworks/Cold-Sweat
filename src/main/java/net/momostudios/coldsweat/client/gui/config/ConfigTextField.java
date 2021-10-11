package net.momostudios.coldsweat.client.gui.config;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;

public class ConfigTextField extends TextFieldWidget
{
    public ConfigTextField(FontRenderer fontRenderer, int x, int y, int width, int height, ITextComponent title) {
        super(fontRenderer, x, y, width, height, title);
    }

    private void onTextChanged(String newText)
    {
        ConfigScreen.INSTANCE.difficulty = 4;
    }
}
