package net.momostudios.coldsweat.client.gui.config;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;

public class DifficultyDescriptions
{
    public static List<ITextComponent> getListFor(int difficulty)
    {
        return  difficulty == 0 ? superEasyDescription() :
                difficulty == 1 ? easyDescription() :
                difficulty == 2 ? normalDescription() :
                difficulty == 3 ? hardDescription() :
                                  customDescription();
    }

    public static List<ITextComponent> superEasyDescription()
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description1"),
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description2"),
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description3"),
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description4"),
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description5"),
                new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.description6")
        );
    }

    public static List<ITextComponent> easyDescription()
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description1"),
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description2"),
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description3"),
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description4"),
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description5"),
                new TranslationTextComponent("cold_sweat.config.difficulty.easy.description6")
        );
    }

    public static List<ITextComponent> normalDescription()
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description1"),
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description2"),
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description3"),
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description4"),
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description5"),
                new TranslationTextComponent("cold_sweat.config.difficulty.normal.description6")
        );
    }

    public static List<ITextComponent> hardDescription()
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description1"),
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description2"),
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description3"),
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description4"),
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description5"),
                new TranslationTextComponent("cold_sweat.config.difficulty.hard.description6")
        );
    }

    public static List<ITextComponent> customDescription()
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.custom.description1")
        );
    }
}
