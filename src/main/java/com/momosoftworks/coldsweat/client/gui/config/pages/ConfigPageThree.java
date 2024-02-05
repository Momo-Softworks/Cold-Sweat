package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class ConfigPageThree extends AbstractConfigPage
{
    public ConfigPageThree(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public ITextComponent sectionOneTitle()
    {   return new TranslationTextComponent("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public ITextComponent sectionTwoTitle()
    {   return null;
    }

    @Override
    protected void init()
    {
        super.init();

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> new TranslationTextComponent("cold_sweat.config.grace_period.name").append(": ").append(ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                       button ->
                       {
                           ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());
                           button.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.grace_period.name").getString() + ": "
                                                            + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                       },
                       true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, new TranslationTextComponent("cold_sweat.config.grace_period_length.name"),
                             value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                             input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                             true, false, false, new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_1"),
                             new TranslationTextComponent("cold_sweat.config.grace_period_length.desc_2").withStyle(TextFormatting.DARK_GRAY));
    }

    @Override
    public void onClose()
    {   super.onClose();
        ConfigScreen.saveConfig();
    }
}
