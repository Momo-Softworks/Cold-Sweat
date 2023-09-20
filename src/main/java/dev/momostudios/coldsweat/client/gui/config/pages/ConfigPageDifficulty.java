package dev.momostudios.coldsweat.client.gui.config.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ConfigSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.momostudios.coldsweat.config.ConfigSettings.Difficulty.*;

public class ConfigPageDifficulty extends Screen
{
    private static final String BLUE = ChatFormatting.BLUE.toString();
    private static final String RED = ChatFormatting.RED.toString();
    private static final String YEL = ChatFormatting.YELLOW.toString();
    private static final String CLEAR = ChatFormatting.RESET.toString();
    private static final String BOLD = ChatFormatting.BOLD.toString();
    private static final String U_LINE = ChatFormatting.UNDERLINE.toString();

    private static final List<Component> SUPER_EASY_DESCRIPTION = generateDescription(SUPER_EASY);
    private static final List<Component> EASY_DESCRIPTION = generateDescription(EASY);
    private static final List<Component> NORMAL_DESCRIPTION = generateDescription(NORMAL);
    private static final List<Component> HARD_DESCRIPTION = generateDescription(HARD);
    private static final List<Component> CUSTOM_DESCRIPTION = Collections.singletonList(
                    new TranslatableComponent("cold_sweat.config.difficulty.description.custom"));

    static final ResourceLocation CONFIG_BUTTONS_LOCATION = new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png");

    private final Screen parentScreen;

    public ConfigPageDifficulty(Screen parentScreen)
    {
        super(new TranslatableComponent("cold_sweat.config.section.difficulty.name"));
        this.parentScreen = parentScreen;
    }

    public static List<Component> getListFor(int difficulty)
    {
        return switch (difficulty)
        {   case 0  -> SUPER_EASY_DESCRIPTION;
            case 1  -> EASY_DESCRIPTION;
            case 2  -> NORMAL_DESCRIPTION;
            case 3  -> HARD_DESCRIPTION;
            default -> CUSTOM_DESCRIPTION;
        };
    }

    private static List<Component> generateDescription(ConfigSettings.Difficulty difficulty)
    {
        return List.of(
                new TranslatableComponent("cold_sweat.config.difficulty.description.min_temp", getTemperatureString(difficulty.getSetting("min_temp"), BLUE)),
                new TranslatableComponent("cold_sweat.config.difficulty.description.max_temp", getTemperatureString(difficulty.getSetting("max_temp"), RED)),
                getRateComponent(difficulty),
                new TranslatableComponent("cold_sweat.config.difficulty.description.world_temp_" + (difficulty.getSetting("require_thermometer") ? "off" : "on"), BOLD + U_LINE, CLEAR),
                new TranslatableComponent("cold_sweat.config.difficulty.description.scaling_" + (difficulty.getSetting("damage_scaling") ? "on" : "off"), BOLD + U_LINE, CLEAR),
                new TranslatableComponent("cold_sweat.config.difficulty.description.potions_" + (difficulty.getSetting("ice_resistance_enabled") ? "on" : "off"), BOLD + U_LINE, CLEAR));
    }

    private static String getTemperatureString(double temp, String color)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        return color + df.format(Temperature.convertUnits(temp, Temperature.Units.MC, Temperature.Units.F, true)) + CLEAR + " °F / "
             + color + df.format(Temperature.convertUnits(temp, Temperature.Units.MC, Temperature.Units.C, true)) + CLEAR + " °C";
    }

    private static Component getRateComponent(ConfigSettings.Difficulty difficulty)
    {
        double rate = difficulty.getSetting("temp_rate");
        String key = rate < 1  ? "cold_sweat.config.difficulty.description.rate.decrease"
                   : rate == 1 ? "cold_sweat.config.difficulty.description.rate.normal"
                   : "cold_sweat.config.difficulty.description.rate.increase";

        return rate == 1 ? new TranslatableComponent(key)
                         : new TranslatableComponent(key, YEL + (Math.abs(1 - rate) * 100) + "%" + CLEAR);
    }

    public static int getDifficultyColor(int difficulty)
    {
        return switch (difficulty)
        {   case 0  -> 16777215;
            case 1  -> 16768882;
            case 2  -> 16755024;
            case 3  -> 16731202;
            default -> 10631158;
        };
    }

    public static String getDifficultyName(int difficulty)
    {
        return switch (difficulty)
        {   case 0  -> new TranslatableComponent("cold_sweat.config.difficulty.super_easy.name").getString();
            case 1  -> new TranslatableComponent("cold_sweat.config.difficulty.easy.name").getString();
            case 2  -> new TranslatableComponent("cold_sweat.config.difficulty.normal.name").getString();
            case 3  -> new TranslatableComponent("cold_sweat.config.difficulty.hard.name").getString();
            default -> new TranslatableComponent("cold_sweat.config.difficulty.custom.name").getString();
        };
    }

    public int index()
    {
        return -1;
    }


    @Override
    protected void init()
    {
        this.addRenderableWidget(new Button(
                this.width / 2 - ConfigScreen.BOTTOM_BUTTON_WIDTH / 2,
                this.height - ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET,
                ConfigScreen.BOTTOM_BUTTON_WIDTH, 20,
                CommonComponents.GUI_DONE,
                button -> this.onClose()));
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        // Render Background
        if (this.minecraft.level != null) {
            this.fillGradient(poseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else {
            this.renderDirtBackground(0);
        }

        int difficulty = ConfigSettings.DIFFICULTY.get();

        // Get a list of TextComponents to render
        List<Component> descLines = new ArrayList<>();
        descLines.add(new TextComponent(""));

        // Get max text length (used to extend the text box if it's too wide)
        int longestLine = 0;
        for (Component text : getListFor(difficulty))
        {
            // Add the text and a new line to the list
            Component descLine = new TextComponent(" • " + text.getString() + " ");
            descLines.add(descLine);
            descLines.add(new TextComponent(""));

            int lineWidth = font.width(descLine);
            if (lineWidth > longestLine)
                longestLine = lineWidth;
        }

        // Draw Text Box
        int middleX = this.width / 2;
        int middleY = this.height / 2;
        this.renderTooltip(poseStack, descLines, ItemStack.EMPTY.getTooltipImage(), middleX - longestLine / 2 - 10, middleY - 16, this.font);

        // Set the mouse's position for ConfigScreen (used for click events)
        ConfigScreen.MOUSE_X = mouseX;
        ConfigScreen.MOUSE_Y = mouseY;

        // Draw Title
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, ConfigScreen.TITLE_HEIGHT, 0xFFFFFF);


        RenderSystem.setShaderTexture(0, CONFIG_BUTTONS_LOCATION);

        // Draw Slider Bar
        this.blit(poseStack, this.width / 2 - 76, this.height / 2 - 53, 12,
                isMouseOverSlider(mouseX, mouseY) ? 134 : 128, 152, 6);

        // Draw Slider Head
        this.blit(poseStack, this.width / 2 - 78 + (difficulty * 37), this.height / 2 - 58,
                isMouseOverSlider(mouseX, mouseY) ? 0 : 6, 128, 6, 16);

        // Draw Difficulty Title
        String difficultyName = getDifficultyName(difficulty);
        this.font.drawShadow(poseStack, difficultyName, this.width / 2.0f - (font.width(difficultyName) / 2f),
                             this.height / 2.0f - 84, getDifficultyColor(difficulty));

        // Render Button(s)
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        switch (ConfigSettings.DIFFICULTY.get())
        {   // Super Easy
            case 0 -> SUPER_EASY.load();
            // Easy
            case 1 -> EASY.load();
            // Normal
            case 2 -> NORMAL.load();
            // Hard
            case 3 -> HARD.load();
        }
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
            int newDifficulty = 0;
            if (x < this.width / 2.0 - 76 + (19))
            {
                newDifficulty = 0;
            }
            else if (x < this.width / 2.0 - 76 + (19 * 3))
            {
                newDifficulty = 1;
            }
            else if (x < this.width / 2.0 - 76 + (19 * 5))
            {
                newDifficulty = 2;
            }
            else if (x < this.width / 2.0 - 76 + (19 * 7))
            {
                newDifficulty = 3;
            }
            else if (x < this.width / 2.0 - 76 + (19 * 9))
            {
                newDifficulty = 4;
            }

            if (newDifficulty != ConfigSettings.DIFFICULTY.get())
            {
                ConfigScreen.MC.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("minecraft:block.note_block.hat")), 1.8f, 0.5f));
            }
            ConfigSettings.DIFFICULTY.set(newDifficulty);
        }
    }
}
