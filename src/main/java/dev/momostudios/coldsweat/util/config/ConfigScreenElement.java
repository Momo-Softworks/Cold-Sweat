package dev.momostudios.coldsweat.util.config;

import dev.momostudios.coldsweat.client.gui.config.ConfigPageBase;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class ConfigScreenElement
{
    public final Component label;
    public final EditBox textBox;
    public final ConfigPageBase.Side side;
    public final boolean requireOP;

    public ConfigScreenElement(EditBox textBox, Component label, ConfigPageBase.Side side, boolean requireOP)
    {
        this.textBox = textBox;
        this.label = label;
        this.side = side;
        this.requireOP = requireOP;
    }
}
