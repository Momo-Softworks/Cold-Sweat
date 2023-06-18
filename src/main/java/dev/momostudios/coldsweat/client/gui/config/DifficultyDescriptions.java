package dev.momostudios.coldsweat.client.gui.config;

import dev.momostudios.coldsweat.api.util.Temperature;
import net.minecraft.ChatFormatting;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class DifficultyDescriptions
{
    private static final String BLUE = ChatFormatting.BLUE.toString();
    private static final String RED = ChatFormatting.RED.toString();
    private static final String YEL = ChatFormatting.YELLOW.toString();
    private static final String CLEAR = ChatFormatting.RESET.toString();
    private static final String BOLD = ChatFormatting.BOLD.toString();
    private static final String U_LINE = ChatFormatting.UNDERLINE.toString();

    private static final List<Component> SUPER_EASY_DESCRIPTION = List.of(
                    Component.translatable("cold_sweat.config.difficulty.description.min_temp", temperatureString(40, BLUE)),
                    Component.translatable("cold_sweat.config.difficulty.description.max_temp", temperatureString(120, RED)),
                    Component.translatable("cold_sweat.config.difficulty.description.rate.decrease", YEL +"50%"+ CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.world_temp_on", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.scaling_off", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.potions_on", BOLD + U_LINE, CLEAR));

    private static final List<Component> EASY_DESCRIPTION = List.of(
                    Component.translatable("cold_sweat.config.difficulty.description.min_temp", temperatureString(45, BLUE)),
                    Component.translatable("cold_sweat.config.difficulty.description.max_temp", temperatureString(110, RED)),
                    Component.translatable("cold_sweat.config.difficulty.description.rate.decrease", YEL +"25%"+ CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.world_temp_on", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.scaling_off", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.potions_on", BOLD + U_LINE, CLEAR));

    private static final List<Component> NORMAL_DESCRIPTION = List.of(
                    Component.translatable("cold_sweat.config.difficulty.description.min_temp", temperatureString(50, BLUE)),
                    Component.translatable("cold_sweat.config.difficulty.description.max_temp", temperatureString(100, RED)),
                    Component.translatable("cold_sweat.config.difficulty.description.rate.decrease", YEL +"10%"+ CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.world_temp_on", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.scaling_off", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.potions_on", BOLD + U_LINE, CLEAR));

    private static final List<Component> HARD_DESCRIPTION = List.of(
                    Component.translatable("cold_sweat.config.difficulty.description.min_temp", temperatureString(55, BLUE)),
                    Component.translatable("cold_sweat.config.difficulty.description.max_temp", temperatureString(90, RED)),
                    Component.translatable("cold_sweat.config.difficulty.description.rate.normal"),
                    Component.translatable("cold_sweat.config.difficulty.description.world_temp_off", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.scaling_on", BOLD + U_LINE, CLEAR),
                    Component.translatable("cold_sweat.config.difficulty.description.potions_on", BOLD + U_LINE, CLEAR));

    private static final List<Component> CUSTOM_DESCRIPTION = Collections.singletonList(
                    Component.translatable("cold_sweat.config.difficulty.description.custom"));

    public static List<Component> getListFor(int difficulty)
    {
        return switch (difficulty)
        {
            case 0  -> SUPER_EASY_DESCRIPTION;
            case 1  -> EASY_DESCRIPTION;
            case 2  -> NORMAL_DESCRIPTION;
            case 3  -> HARD_DESCRIPTION;
            default  -> CUSTOM_DESCRIPTION;
        };
    }

    private static String temperatureString(double temp, String color)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        return color + temp + CLEAR + " °F / " + color + df.format(CSMath.convertTemp(temp, Temperature.Units.F, Temperature.Units.C, true)) + CLEAR + " °C";
    }
}
