package dev.momostudios.coldsweat.client.gui.config.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigPageDifficulty extends Screen
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
                    Component.translatable("cold_sweat.config.difficulty.description.scaling_on", BOLD + U_LINE, CLEAR),
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

    static final ResourceLocation CONFIG_BUTTONS_LOCATION = new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png");

    private final Screen parentScreen;

    public ConfigPageDifficulty(Screen parentScreen)
    {
        super(Component.translatable("cold_sweat.config.section.difficulty.name"));
        this.parentScreen = parentScreen;
    }

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

    public static int difficultyColor(int difficulty)
    {
        return  difficulty == 0 ? 16777215 :
                difficulty == 1 ? 16768882 :
                difficulty == 2 ? 16755024 :
                difficulty == 3 ? 16731202 :
                difficulty == 4 ? 10631158 : 16777215;
    }

    public static String difficultyName(int difficulty)
    {
        return  difficulty == 0 ? Component.translatable("cold_sweat.config.difficulty.super_easy.name").getString() :
                difficulty == 1 ? Component.translatable("cold_sweat.config.difficulty.easy.name").getString() :
                difficulty == 2 ? Component.translatable("cold_sweat.config.difficulty.normal.name").getString() :
                difficulty == 3 ? Component.translatable("cold_sweat.config.difficulty.hard.name").getString() :
                difficulty == 4 ? Component.translatable("cold_sweat.config.difficulty.custom.name").getString() : "";
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
                button -> this.close()));
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
        descLines.add(Component.literal(""));

        // Get max text length (used to extend the text box if it's too wide)
        int longestLine = 0;
        for (Component text : getListFor(difficulty))
        {
            // Add the text and a new line to the list
            Component descLine = Component.literal(" • " + text.getString() + " ");
            descLines.add(descLine);
            descLines.add(Component.literal(""));

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
        String difficultyName = difficultyName(difficulty);
        this.font.drawShadow(poseStack, difficultyName, this.width / 2.0f - (font.width(difficultyName) / 2f),
                             this.height / 2.0f - 84, difficultyColor(difficulty));

        // Render Button(s)
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private void close()
    {
        switch (ConfigSettings.DIFFICULTY.get())
        {
            // Super Easy
            case 0 ->
            {
                ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(40, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(120, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.TEMP_RATE.set(0.5);
                ConfigSettings.REQUIRE_THERMOMETER.set(false);
                ConfigSettings.DAMAGE_SCALING.set(false);
                ConfigSettings.FIRE_RESISTANCE_ENABLED.set(true);
                ConfigSettings.ICE_RESISTANCE_ENABLED.set(true);
            }
            // Easy
            case 1 ->
            {
                ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(45, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(110, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.TEMP_RATE.set(0.75);
                ConfigSettings.REQUIRE_THERMOMETER.set(false);
                ConfigSettings.DAMAGE_SCALING.set(false);
                ConfigSettings.FIRE_RESISTANCE_ENABLED.set(true);
                ConfigSettings.ICE_RESISTANCE_ENABLED.set(true);
            }
            // Normal
            case 2 ->
            {
                ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(50, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(100, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.TEMP_RATE.set(1.0);
                ConfigSettings.REQUIRE_THERMOMETER.set(true);
                ConfigSettings.DAMAGE_SCALING.set(true);
                ConfigSettings.FIRE_RESISTANCE_ENABLED.set(true);
                ConfigSettings.ICE_RESISTANCE_ENABLED.set(true);
            }
            // Hard
            case 3 ->
            {
                ConfigSettings.MIN_TEMP.set(CSMath.convertTemp(60, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.MAX_TEMP.set(CSMath.convertTemp(90, Temperature.Units.F, Temperature.Units.MC, true));
                ConfigSettings.TEMP_RATE.set(1.5);
                ConfigSettings.REQUIRE_THERMOMETER.set(true);
                ConfigSettings.DAMAGE_SCALING.set(true);
                ConfigSettings.FIRE_RESISTANCE_ENABLED.set(false);
                ConfigSettings.ICE_RESISTANCE_ENABLED.set(false);
            }
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
