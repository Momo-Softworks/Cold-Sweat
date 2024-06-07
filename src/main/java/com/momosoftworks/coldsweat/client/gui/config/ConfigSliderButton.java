package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class ConfigSliderButton extends AbstractSliderButton
{
    public ConfigSliderButton(int x, int y, int width, int height, Component message, double value)
    {   super(x, y, width, height, message, value);
    }

    @Override
    protected void updateMessage()
    {
    }

    @Override
    protected void applyValue()
    {
    }

    public void setValue(double value)
    {   this.value = CSMath.clamp(value, 0, 1);
    }

    public double getValue()
    {   return this.value;
    }

    public void setMessagePercentage(MutableComponent message, double value, boolean offAtZero)
    {   this.setMessage(message.append(": ").append(value > 0 || !offAtZero ? new TextComponent((int) (value * 100) + "%") : new TextComponent(CommonComponents.OPTION_OFF.getString())));
    }
}
