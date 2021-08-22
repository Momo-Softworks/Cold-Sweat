package net.momostudios.coldsweat.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import joptsimple.OptionDescriptor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.gui.widget.button.OptionButton;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.settings.BooleanOption;
import net.minecraft.client.settings.IteratableOption;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.momostudios.coldsweat.config.ColdSweatConfig;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Objects;

public class ConfigScreen extends Screen
{
    /*
     * Screen display parameters
     */
    private static final int TITLE_HEIGHT = 16;
    private static final int OPTIONS_TOP = 24;
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;
    private static final int OPTION_SIZE = 25;
    private static final int BOTTOM_BUTTON_WIDTH = 150;

    private static final ColdSweatConfig CMI = ColdSweatConfig.getInstance();

    boolean celsius = CMI.celsius();

    private final Screen parentScreen;
    OptionsRowList optionsRowList;
    ResourceLocation divider = new ResourceLocation("cold_sweat:textures/gui/screen/configs/style_divider.png");

    TextFieldWidget tempOffsetInput;
    TextFieldWidget maxTempInput;
    TextFieldWidget minTempInput;
    TextFieldWidget rateMultInput;
    Button celsiusButton;

    ImageButton upSteveButton;
    ImageButton downSteveButton;
    ImageButton rightSteveButton;
    ImageButton leftSteveButton;

    public ConfigScreen(Screen parentScreen)
    {
        super(new TranslationTextComponent("cold_sweat.config.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init()
    {
        this.optionsRowList = new OptionsRowList(
                Objects.requireNonNull(this.minecraft), this.width, this.height,
                OPTIONS_TOP,
                this.height - OPTIONS_LIST_BOTTOM_OFFSET,
                OPTION_SIZE);
        this.addButton(new Button(
                this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
                this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
                BOTTOM_BUTTON_WIDTH, 20,
                new TranslationTextComponent("gui.done"),
                button -> this.close())
        );

        // The options

        // Celsius
        celsiusButton = new Button(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
            new StringTextComponent(new TranslationTextComponent("cold_sweat.config.celsius.name").getString() + ": " + (this.celsius ? "ON" : "OFF")),
            button -> this.toggleCelsius());
        this.addButton(celsiusButton);

        // Temp Offset
        this.tempOffsetInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 20, 51, 22, new StringTextComponent(""));
        this.tempOffsetInput.setText(String.valueOf(celsius ? (CMI.tempOffset() * 42) * 5/2 : CMI.tempOffset() * 42));

        // Max Temperature
        this.maxTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 52, 51, 22, new StringTextComponent(""));
        this.maxTempInput.setText(String.valueOf(celsius ? (int) (((CMI.maxHabitable() * 42 + 32) - 32) / 1.8) : (int) (CMI.maxHabitable() * 42 + 32)));

        // Min Temperature
        this.minTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 84, 51, 22, new StringTextComponent(""));
        this.minTempInput.setText(String.valueOf(celsius ? (int) (((CMI.minHabitable() * 42 + 32) - 32) / 1.8) : (int) (CMI.minHabitable() * 42 + 32)));

        // Rate Multiplier
        this.rateMultInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 116, 51, 22, new StringTextComponent(""));
        this.rateMultInput.setText(String.valueOf(CMI.rateMultiplier()));


        // Direction Buttons: Steve Head
        leftSteveButton = new ImageButton(this.width / 2 + 51, this.height / 4 + 2, 20, 20, 60, 0, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/direction_buttons.png"), button -> changeSelfIndicatorPos(0, -1));
        this.addButton(leftSteveButton);
        upSteveButton = new ImageButton(this.width / 2 + 71, this.height / 4 - 8, 20, 20, 20, 0, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/direction_buttons.png"), button -> changeSelfIndicatorPos(1, -1));
        this.addButton(upSteveButton);
        downSteveButton = new ImageButton(this.width / 2 + 71, this.height / 4 + 12, 20, 20, 40, 0, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/direction_buttons.png"), button -> changeSelfIndicatorPos(1, 1));
        this.addButton(downSteveButton);
        rightSteveButton = new ImageButton(this.width / 2 + 91, this.height / 4 + 2, 20, 20, 0, 0, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/direction_buttons.png"), button -> changeSelfIndicatorPos(0, 1));
        this.addButton(rightSteveButton);

        this.children.add(this.tempOffsetInput);
        this.children.add(this.maxTempInput);
        this.children.add(this.minTempInput);
        this.children.add(this.rateMultInput);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        //this.optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        /*
         * Render Section Titles
         */

        // Temp Management
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.section.temperature_details"), this.width / 2 - 204, this.height / 4 - 28, 16777215);

        Minecraft.getInstance().getTextureManager().bindTexture(divider);
        this.blit(matrixStack, this.width / 2 - 202, this.height / 4 - 16, 0, 0, 1, 155);

        // UI Positions
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.section.ui_position"), this.width / 2 + 32, this.height / 4 - 28, 16777215);

        Minecraft.getInstance().getTextureManager().bindTexture(divider);
        this.blit(matrixStack, this.width / 2 + 34, this.height / 4 - 16, 0, 0, 1, 155);

        /*
         * Render options
         */

        // Buttons
        this.celsiusButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.upSteveButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.downSteveButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.leftSteveButton.render(matrixStack, mouseX, mouseY, partialTicks);
        this.rightSteveButton.render(matrixStack, mouseX, mouseY, partialTicks);

        // Temp Offset
        this.tempOffsetInput.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.temp_offset.name"), this.width / 2 - 185, tempOffsetInput.y + 6, 16777215);

        // Max Temp
        this.maxTempInput.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.max_temperature.name"), this.width / 2 - 185, maxTempInput.y + 6, 16777215);

        // Min Temp
        this.minTempInput.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.min_temperature.name"), this.width / 2 - 185, minTempInput.y + 6, 16777215);

        // Rate Multiplier
        this.rateMultInput.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.rate_multiplier.name"), this.width / 2 - 185, rateMultInput.y + 6, 16777215);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        tempOffsetInput.tick();
        maxTempInput.tick();
        minTempInput.tick();
        rateMultInput.tick();
    }

    public boolean isShiftPressed()
    {
        return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 340);
    }


    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public void close()
    {
        this.onClose();
        Objects.requireNonNull(this.minecraft).displayGuiScreen(parentScreen);
    }

    @Override
    public void onClose()
    {
        CMI.setCelsius(this.celsius);

        try
        {
            CMI.setTempOffset(Integer.parseInt(tempOffsetInput.getText()));
        } catch (Exception e) {}

        try
        {
            double maxTemp = Integer.parseInt(maxTempInput.getText());
            CMI.setMaxHabitable(celsius ? ((maxTemp * 1.8 + 32.0) - 32.0) / 42.0 : (maxTemp - 32.0) / 42.0);
        } catch (Exception e) {}

        try
        {
            double minTemp = Integer.parseInt(minTempInput.getText());
            CMI.setMinHabitable(celsius ? ((minTemp * 1.8 + 32.0) - 42.0) / 32.0 : (minTemp - 32.0) / 42.0);
        } catch (Exception e) {}

        try
        {
            double rateModifier = Double.parseDouble(rateMultInput.getText());
            CMI.setRateMultiplier(rateModifier);
        } catch (Exception e) {}

        CMI.save();
    }

    public void toggleCelsius()
    {
        this.celsius = !this.celsius;
        celsiusButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.celsius.name").getString() + ": " +
            (this.celsius ? "ON" : "OFF")));
    }

    public void changeSelfIndicatorPos(int axis, int amount)
    {
        if (isShiftPressed()) amount *= 10;
        if (axis == 0)
        {
            CMI.setSteveHeadX(CMI.steveHeadX() + amount);
        }
        else if (axis == 1)
        {
            CMI.setSteveHeadY(CMI.steveHeadY() + amount);
        }
        CMI.save();
    }
}
