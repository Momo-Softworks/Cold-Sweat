package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.momosoftworks.coldsweat.client.gui.config.AbstractConfigPage;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class ConfigPageThree extends AbstractConfigPage
{
    public ConfigPageThree(Screen parentScreen)
    {   super(parentScreen);
    }

    @Override
    public Component sectionOneTitle()
    {   return Component.translatable("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public Component sectionTwoTitle()
    {   return null;
    }

    @Override
    protected void init()
    {
        super.init();

        // Enable Grace Period
        this.addButton("grace_toggle", Side.LEFT, () -> Component.translatable("cold_sweat.config.grace_period.name").append(": ").append(ConfigSettings.GRACE_ENABLED.get() ? ON : OFF),
                       button ->
                       {
                           ConfigSettings.GRACE_ENABLED.set(!ConfigSettings.GRACE_ENABLED.get());
                           button.setMessage(Component.literal(Component.translatable("cold_sweat.config.grace_period.name").getString() + ": "
                                                                       + (ConfigSettings.GRACE_ENABLED.get() ? ON : OFF)));
                       },
                       true, false, false, Component.translatable("cold_sweat.config.grace_period.desc"));

        // Grace Period Length
        this.addDecimalInput("grace_length", Side.LEFT, Component.translatable("cold_sweat.config.grace_period_length.name"),
                             value -> ConfigSettings.GRACE_LENGTH.set(value.intValue()),
                             input -> input.setValue(ConfigSettings.GRACE_LENGTH.get() + ""),
                             true, false, false, Component.translatable("cold_sweat.config.grace_period_length.desc_1"),
                             Component.translatable("cold_sweat.config.grace_period_length.desc_2").withStyle(ChatFormatting.DARK_GRAY));
    }
}
