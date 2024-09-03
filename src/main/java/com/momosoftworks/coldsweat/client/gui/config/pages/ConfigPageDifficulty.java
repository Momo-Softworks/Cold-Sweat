package com.momosoftworks.coldsweat.client.gui.config.pages;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.config.ConfigScreen;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.*;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.momosoftworks.coldsweat.config.ConfigSettings.Difficulty.*;

public class ConfigPageDifficulty extends Screen
{
    private static final String BLUE = TextFormatting.BLUE.toString();
    private static final String RED = TextFormatting.RED.toString();
    private static final String YEL = TextFormatting.YELLOW.toString();
    private static final String CLEAR = TextFormatting.RESET.toString();
    private static final String BOLD = TextFormatting.BOLD.toString();
    private static final String U_LINE = TextFormatting.UNDERLINE.toString();

    private static final List<TextComponent> SUPER_EASY_DESCRIPTION = generateDescription(SUPER_EASY);
    private static final List<TextComponent> EASY_DESCRIPTION = generateDescription(EASY);
    private static final List<TextComponent> NORMAL_DESCRIPTION = generateDescription(NORMAL);
    private static final List<TextComponent> HARD_DESCRIPTION = generateDescription(HARD);
    private static final List<TextComponent> CUSTOM_DESCRIPTION = Collections.singletonList(
                    new TranslationTextComponent("cold_sweat.config.difficulty.description.custom"));

    static final ResourceLocation CONFIG_BUTTONS_LOCATION = new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png");

    private final Screen parentScreen;

    public ConfigPageDifficulty(Screen parentScreen)
    {
        super(new TranslationTextComponent("cold_sweat.config.section.difficulty"));
        this.parentScreen = parentScreen;
    }

    public static List<TextComponent> getListFor(ConfigSettings.Difficulty difficulty)
    {
        switch (difficulty)
        {   case SUPER_EASY : return SUPER_EASY_DESCRIPTION;
            case EASY   : return EASY_DESCRIPTION;
            case NORMAL : return NORMAL_DESCRIPTION;
            case HARD   : return HARD_DESCRIPTION;
            default : return CUSTOM_DESCRIPTION;
        }
    }

    private static List<TextComponent> generateDescription(ConfigSettings.Difficulty difficulty)
    {
        return Arrays.asList(
                new TranslationTextComponent("cold_sweat.config.difficulty.description.min_temp", getTemperatureString(difficulty.getSetting("min_temp"), BLUE)),
                new TranslationTextComponent("cold_sweat.config.difficulty.description.max_temp", getTemperatureString(difficulty.getSetting("max_temp"), RED)),
                new StringTextComponent(getRateComponent(difficulty).getString()),
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.difficulty.description.world_temp_"
                                                                  + (difficulty.getSetting("require_thermometer") ? "off" : "on"), BOLD + U_LINE, CLEAR).getString()),
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.difficulty.description.potions_"
                                                                  + (difficulty.getSetting("ice_resistance_enabled") ? "on" : "off"), BOLD + U_LINE, CLEAR).getString()));
    }

    private static String getTemperatureString(double temp, String color)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        return color + df.format(Temperature.convert(temp, Temperature.Units.MC, Temperature.Units.F, true)) + CLEAR + " \u00B0F / "
             + color + df.format(Temperature.convert(temp, Temperature.Units.MC, Temperature.Units.C, true)) + CLEAR + " \u00B0C";
    }

    private static TextComponent getRateComponent(ConfigSettings.Difficulty difficulty)
    {
        double rate = difficulty.getSetting("temp_rate");
        String key = rate < 1  ? "cold_sweat.config.difficulty.description.rate.decrease"
                   : rate == 1 ? "cold_sweat.config.difficulty.description.rate.normal"
                   : "cold_sweat.config.difficulty.description.rate.increase";

        return rate == 1 ? new TranslationTextComponent(key)
                         : new TranslationTextComponent(key, YEL + (Math.abs(1 - rate) * 100) + "%" + CLEAR);
    }

    public static int getDifficultyColor(ConfigSettings.Difficulty difficulty)
    {
        switch (difficulty)
        {   case SUPER_EASY : return 16777215;
            case EASY   : return 16768882;
            case NORMAL : return 16755024;
            case HARD   : return 16731202;
            default : return 10631158;
        }
    }

    public int index()
    {
        return -1;
    }

    @Override
    protected void init()
    {
        this.addWidget(new Button(
                this.width / 2 - ConfigScreen.BOTTOM_BUTTON_WIDTH / 2,
                this.height - ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET,
                ConfigScreen.BOTTOM_BUTTON_WIDTH, 20,
                DialogTexts.GUI_DONE,
                button -> this.onClose()));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        // Render Background
        if (this.minecraft.level != null)
        {   this.fillGradient(matrixStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else
        {   this.renderDirtBackground(0);
        }

        ConfigSettings.Difficulty difficulty = ConfigSettings.DIFFICULTY.get();

        // Get a list of TextComponents to render
        List<IFormattableTextComponent> descLines = new ArrayList<>();
        descLines.add(new StringTextComponent(""));

        // Get max text length (used to extend the text box if it's too wide)
        int longestLine = 0;
        for (TextComponent text : getListFor(difficulty))
        {
            // Add the text and a new line to the list
            IFormattableTextComponent descLine = new StringTextComponent(" \u2022 ").append(text).append(" ");
            descLines.add(descLine);
            descLines.add(new StringTextComponent(""));

            int lineWidth = font.width(descLine);
            if (lineWidth > longestLine)
                longestLine = lineWidth;
        }

        // Draw Text Box
        int middleX = this.width / 2;
        int middleY = this.height / 2;
        this.renderWrappedToolTip(matrixStack, descLines, middleX - longestLine / 2 - 10, middleY - 16, this.font);

        // Set the mouse's position for ConfigScreen (used for click events)
        ConfigScreen.MOUSE_X = mouseX;
        ConfigScreen.MOUSE_Y = mouseY;

        // Draw Title
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, ConfigScreen.TITLE_HEIGHT, 0xFFFFFF);


         this.minecraft.textureManager.bind(CONFIG_BUTTONS_LOCATION);

        // Draw Slider Bar
        this.blit(matrixStack, this.width / 2 - 76, this.height / 2 - 53, 12,
                isMouseOverSlider(mouseX, mouseY) ? 134 : 128, 152, 6);

        // Draw Slider Head
        this.blit(matrixStack, this.width / 2 - 78 + (difficulty.getId() * 37), this.height / 2 - 58,
                isMouseOverSlider(mouseX, mouseY) ? 0 : 6, 128, 6, 16);

        // Draw Difficulty Title
        IFormattableTextComponent difficultyName = getFormattedName(difficulty);
        this.font.drawShadow(matrixStack, difficultyName, this.width / 2.0f - (font.width(difficultyName) / 2f),
                             this.height / 2.0f - 84, getDifficultyColor(difficulty));

        // Render Button(s)
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        ConfigSettings.DIFFICULTY.get().load();
        ConfigScreen.saveConfig();
        ConfigScreen.MC.setScreen(parentScreen);
    }

    boolean isMouseOverSlider(double mouseX, double mouseY)
    {
        return (mouseX >= this.width / 2.0 - 80 && mouseX <= this.width / 2.0 + 80 &&
                mouseY >= this.height / 2.0 - 67 && mouseY <= this.height / 2.0 - 35);
    }

    @Override
    public void tick()
    {
        double x = ConfigScreen.MOUSE_X;
        double y = ConfigScreen.MOUSE_Y;
        if (ConfigScreen.IS_MOUSE_DOWN && isMouseOverSlider(x, y))
        {
            ConfigSettings.Difficulty newDifficulty = ConfigSettings.Difficulty.byId((int) Math.round(CSMath.blend(0, 4, x, this.width / 2.0 - 76, this.width / 2.0 + 76)));

            if (newDifficulty != ConfigSettings.DIFFICULTY.get())
            {   ConfigScreen.MC.getSoundManager().play(SimpleSound.forUI(new SoundEvent(new ResourceLocation("minecraft:block.note_block.hat")), 1.8f, 0.5f));
            }
            ConfigSettings.DIFFICULTY.set(newDifficulty);
        }
    }
}
