package dev.momostudios.coldsweat.client.gui.config.pages;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.client.gui.config.DifficultyDescriptions;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
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
import java.util.ArrayList;
import java.util.List;

public class ConfigPageDifficulty extends Screen
{
    private final Screen parentScreen;

    private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
    private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;

    static final ResourceLocation CONFIG_BUTTONS_LOCATION = new ResourceLocation("cold_sweat:textures/gui/screen/config_gui.png");

    public ConfigPageDifficulty(Screen parentScreen)
    {
        super(new TranslatableComponent("cold_sweat.config.section.difficulty.name"));
        this.parentScreen = parentScreen;
    }

    public int index()
    {
        return -1;
    }


    @Override
    protected void init()
    {
        this.addRenderableWidget(new Button(
                this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
                this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
                BOTTOM_BUTTON_WIDTH, 20,
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

        // Get a list of TextComponents to render
        List<Component> descLines = new ArrayList<>();
        descLines.add(new TextComponent(""));

        // Get max text length (used to extend the text box if it's too wide)
        int longestLine = 0;
        for (Component text : DifficultyDescriptions.getListFor(ConfigSettings.DIFFICULTY.get()))
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
        drawCenteredString(poseStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);


        RenderSystem.setShaderTexture(0, CONFIG_BUTTONS_LOCATION);

        // Draw Slider Bar
        this.blit(poseStack, this.width / 2 - 76, this.height / 2 - 53, 12,
                isMouseOverSlider(mouseX, mouseY) ? 134 : 128, 152, 6);

        // Draw Slider Head
        this.blit(poseStack, this.width / 2 - 78 + (ConfigSettings.DIFFICULTY.get() * 37), this.height / 2 - 58,
                isMouseOverSlider(mouseX, mouseY) ? 0 : 6, 128, 6, 16);

        // Draw Difficulty Title
        String difficultyName = ConfigScreen.difficultyName(ConfigSettings.DIFFICULTY.get());
        this.font.drawShadow(poseStack, difficultyName, this.width / 2.0f - (font.width(difficultyName) / 2f),
                this.height / 2.0f - 84, ConfigScreen.difficultyColor(ConfigSettings.DIFFICULTY.get()));

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
