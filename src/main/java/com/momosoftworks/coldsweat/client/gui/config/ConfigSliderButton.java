package com.momosoftworks.coldsweat.client.gui.config;

import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.settings.SliderPercentageOption;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class ConfigSliderButton extends Slider
{
    public ConfigSliderButton(int xPos, int yPos, ITextComponent displayStr, double minVal, double maxVal, double currentVal, IPressable handler, ISlider par)
    {   super(xPos, yPos, displayStr, minVal, maxVal, currentVal, handler, par);
    }

    public void setValue(double value)
    {   this.sliderValue = CSMath.clamp(value, 0, 1);
    }

    public double getValue()
    {   return this.sliderValue;
    }

    public void setMessagePercentage(IFormattableTextComponent message, double value, boolean offAtZero)
    {   this.setMessage(message.append(": ").append(value > 0 || !offAtZero ? new StringTextComponent((int) (value * 100) + "%") : new StringTextComponent(DialogTexts.OPTION_OFF.getString())));
    }
}
