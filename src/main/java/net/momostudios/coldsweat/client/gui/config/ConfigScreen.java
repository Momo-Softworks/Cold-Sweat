package net.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.momostudios.coldsweat.config.ColdSweatConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ConfigScreen
{
    public static final int TITLE_HEIGHT = 16;
    public static final int OPTIONS_TOP = 24;
    public static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    public static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;
    public static final int OPTION_SIZE = 25;
    public static final int BOTTOM_BUTTON_WIDTH = 150;
    private static final String ON = new TranslationTextComponent("options.on").getString();
    private static final String OFF = new TranslationTextComponent("options.off").getString();

    private static final ColdSweatConfig CMI = ColdSweatConfig.getInstance();
    public static final ConfigScreen INSTANCE = new ConfigScreen();
    public Screen parentScreen = new OptionsScreen(new IngameMenuScreen(true), Minecraft.getInstance().gameSettings);
    public static Minecraft mc = Minecraft.getInstance();

    public static int FIRST_PAGE = 0;
    public static int LAST_PAGE = 1;

    public static Screen getPage(int index)
    {
        index = Math.max(FIRST_PAGE, Math.min(LAST_PAGE, index));
        switch (index)
        {
            case 0:  return new PageOne();
            case 1:  return new PageTwo();
            default: return null;
        }
    }

    public static class PageOne extends ConfigPageBase
    {
        boolean celsius = CMI.celsius();
        boolean iceRes = CMI.iceResistanceEffect();
        boolean fireRes = CMI.fireResistanceEffect();
        boolean animalTemp = CMI.animalsTemperature();
        boolean damageScaling = CMI.damageScaling();
        boolean requireThermometer = CMI.requireThermometer();

        TextFieldWidget tempOffsetInput;
        TextFieldWidget maxTempInput;
        TextFieldWidget minTempInput;
        TextFieldWidget rateMultInput;
        Button celsiusButton;
        Button iceResButton;
        Button fireResButton;
        Button animalTempButton;
        Button damageScalingButton;
        Button requireThermometerButton;

        Screen parentScreen;

        public PageOne()
        {
            super();
            this.parentScreen = ConfigScreen.INSTANCE.parentScreen;
        }

        @Override
        public int index()
        {
            return 0;
        }

        @Override
        protected void init()
        {
            super.init();

            // The options

            // Celsius
            celsiusButton = new Button(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.celsius.name").getString() + ": " + (this.celsius ? ON : OFF)),
                button -> this.toggleCelsius());
            this.addButton(celsiusButton);

            // Temp Offset
            this.tempOffsetInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 20, 51, 22, new StringTextComponent(""));
            this.tempOffsetInput.setText(String.valueOf(CMI.tempOffset()));

            // Max Temperature
            this.maxTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 52, 51, 22, new StringTextComponent(""));
            this.maxTempInput.setText(String.valueOf(celsius ? (int) (((CMI.maxHabitable() * 42 + 32) - 32) / 1.8) : (int) (CMI.maxHabitable() * 42 + 32)));

            // Min Temperature
            this.minTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 84, 51, 22, new StringTextComponent(""));
            this.minTempInput.setText(String.valueOf(celsius ? (int) (((CMI.minHabitable() * 42 + 32) - 32) / 1.8) : (int) (CMI.minHabitable() * 42 + 32)));

            // Rate Multiplier
            this.rateMultInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 116, 51, 22, new StringTextComponent(""));
            this.rateMultInput.setText(String.valueOf(CMI.rateMultiplier()));

            // Misc. Temp Effects
            iceResButton = new Button(this.width / 2 + 51, this.height / 4 - 8, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (this.iceRes ? ON : OFF)),
                button -> this.toggleIceRes());
            this.addButton(iceResButton);

            fireResButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (this.fireRes ? ON : OFF)),
                button -> this.toggleFireRes());
            this.addButton(fireResButton);

            requireThermometerButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 2, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (this.requireThermometer ? ON : OFF)),
                button -> this.toggleRequireThermometer());
            this.addButton(requireThermometerButton);

            animalTempButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.animal_temperature.name").getString() + ": " + (this.animalTemp ? ON : OFF)),
                button -> this.toggleAnimalTemp());
            this.addButton(animalTempButton);

            damageScalingButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (this.damageScaling ? ON : OFF)),
                button -> this.toggleDamageScaling());
            this.addButton(damageScalingButton);

            this.children.add(this.tempOffsetInput);
            this.children.add(this.maxTempInput);
            this.children.add(this.minTempInput);
            this.children.add(this.rateMultInput);
        }

        @Override
        public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            /*
             * Render config options
             */

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

            //super.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void tick()
        {
            super.tick();
            tempOffsetInput.tick();
            maxTempInput.tick();
            minTempInput.tick();
            rateMultInput.tick();
        }

        @Override
        public void onClose()
        {
            CMI.setCelsius(this.celsius);
            CMI.setIceResistanceEffect(this.iceRes);
            CMI.setFireResistanceEffect(this.fireRes);
            CMI.setAnimalsTemperature(this.animalTemp);
            CMI.setDamageScaling(this.damageScaling);
            CMI.setRequireThermometer(this.requireThermometer);

            try
            {
                CMI.setTempOffset(Integer.parseInt(tempOffsetInput.getText()));
            } catch (Exception e) {}

            try
            {
                double maxTemp = Double.parseDouble(maxTempInput.getText());
                CMI.setMaxHabitable(celsius ? ((maxTemp * 1.8 + 32.0) - 32.0) / 42.0 : (maxTemp - 32.0) / 42.0);
            } catch (Exception e) {}

            try
            {
                double minTemp = Double.parseDouble(minTempInput.getText());
                CMI.setMinHabitable(celsius ? ((minTemp * 1.8 + 32.0) - 32.0) / 42.0 : (minTemp - 32.0) / 42.0);
            } catch (Exception e) {}

            try
            {
                double rateModifier = Double.parseDouble(rateMultInput.getText());
                CMI.setRateMultiplier(rateModifier);
            } catch (Exception e) {}

            super.onClose();
        }

        public void toggleCelsius()
        {
            this.celsius = !this.celsius;
            celsiusButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.celsius.name").getString() + ": " +
                (this.celsius ? ON : OFF)));
        }

        public void toggleIceRes()
        {
            this.iceRes = !this.iceRes;
            iceResButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.ice_resistance.name").getString() + ": " +
                (this.iceRes ? ON : OFF)));
        }

        public void toggleFireRes()
        {
            this.fireRes = !this.fireRes;
            fireResButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.fire_resistance.name").getString() + ": " +
                (this.fireRes ? ON : OFF)));
        }
        public void toggleAnimalTemp()
        {
            this.animalTemp = !this.animalTemp;
            animalTempButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.animal_temperature.name").getString() + ": " +
                (this.animalTemp ? ON : OFF)));
        }
        public void toggleDamageScaling()
        {
            this.damageScaling = !this.damageScaling;
            damageScalingButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.damage_scaling.name").getString() + ": " +
                (this.damageScaling ? ON : OFF)));
        }

        public void toggleRequireThermometer()
        {
            this.requireThermometer = !this.requireThermometer;
            requireThermometerButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.require_thermometer.name").getString() + ": " +
                (this.requireThermometer ? ON : OFF)));
        }
    }

    public static class PageTwo extends ConfigPageBase
    {
        private final Screen parentScreen;

        boolean customHotbar = CMI.customHotbar();
        boolean iconBobbing = CMI.iconBobbing();

        OptionsRowList optionsRowList;
        ResourceLocation divider = new ResourceLocation("cold_sweat:textures/gui/screen/configs/style_divider.png");

        ImageButton upSteveButton;
        ImageButton downSteveButton;
        ImageButton rightSteveButton;
        ImageButton leftSteveButton;
        ImageButton resetSteveButton;

        ImageButton upTempReadoutButton;
        ImageButton downTempReadoutButton;
        ImageButton rightTempReadoutButton;
        ImageButton leftTempReadoutButton;
        ImageButton resetTempReadoutButton;

        Button customHotbarButton;
        Button iconBobbingButton;


        public PageTwo()
        {
            super();
            this.parentScreen = ConfigScreen.INSTANCE.parentScreen;
        }

        @Override
        public int index()
        {
            return 1;
        }

        @Override
        public ITextComponent sectionOneTitle() {
            return new TranslationTextComponent("cold_sweat.config.section.other");
        }

        @Nullable
        @Override
        public ITextComponent sectionTwoTitle() {
            return new TranslationTextComponent("cold_sweat.config.section.hud_settings");
        }

        @Override
        protected void init()
        {
            super.init();

            // The options

            // Direction Buttons: Steve Head
            leftSteveButton = new ImageButton(this.width / 2 + 140, this.height / 4 - 8, 14, 20, 0, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(0, -1));
            this.addButton(leftSteveButton);
            upSteveButton = new ImageButton(this.width / 2 + 154, this.height / 4 - 8, 20, 10, 14, 0, 10,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(1, -1));
            this.addButton(upSteveButton);
            downSteveButton = new ImageButton(this.width / 2 + 154, this.height / 4 + 2, 20, 10, 14, 20, 10,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(1, 1));
            this.addButton(downSteveButton);
            rightSteveButton = new ImageButton(this.width / 2 + 174, this.height / 4 - 8, 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(0, 1));
            this.addButton(rightSteveButton);
            resetSteveButton = new ImageButton(this.width / 2 + 192, this.height / 4 - 8, 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetSelfIndicatorPos());
            this.addButton(resetSteveButton);

            // Direction Buttons: Temp Readout
            leftTempReadoutButton = new ImageButton(this.width / 2 + 140, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 14, 20, 0, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, -1));
            this.addButton(leftTempReadoutButton);
            upTempReadoutButton = new ImageButton(this.width / 2 + 154, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 0, 10,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, -1));
            this.addButton(upTempReadoutButton);
            downTempReadoutButton = new ImageButton(this.width / 2 + 154, this.height / 4 + 2 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 20, 10,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, 1));
            this.addButton(downTempReadoutButton);
            rightTempReadoutButton = new ImageButton(this.width / 2 + 174, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, 1));
            this.addButton(rightTempReadoutButton);
            resetTempReadoutButton = new ImageButton(this.width / 2 + 192, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetTempReadoutPos());
            this.addButton(resetTempReadoutButton);

            // Custom Hotbar
            customHotbarButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (this.customHotbar ? ON : OFF)),
                button -> this.toggleCustomHotbar());
            this.addButton(customHotbarButton);

            // Icon Bobbing
            iconBobbingButton = new Button(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " + (this.iconBobbing ? ON : OFF)),
                button -> this.toggleIconBobbing());
            this.addButton(iconBobbingButton);
        }

        @Override
        public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);

            drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.temperature_icon.name"), this.width / 2 + 51, this.height / 4 - 2, 16777215);
            drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.temperature_readout.name"), this.width / 2 + 51, this.height / 4 - 2 + (int) (OPTION_SIZE * 1.5), 16777215);
            drawString(matrixStack, this.font, new TranslationTextComponent("cold_sweat.config.offset_shift.name"), this.width / 2 + 51, this.height / 4 + 128, 16777215);
        }

        public boolean isShiftPressed()
        {
            return InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 340);
        }

        public void close()
        {
            this.onClose();
            Objects.requireNonNull(this.minecraft).displayGuiScreen(parentScreen);
        }

        @Override
        public void onClose()
        {
            CMI.save();
        }

        private void changeSelfIndicatorPos(int axis, int amount)
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
            super.onClose();
        }

        private void resetSelfIndicatorPos()
        {
            CMI.setSteveHeadX(0);
            CMI.setSteveHeadY(0);
            CMI.save();
        }

        private void changeTempReadoutPos(int axis, int amount)
        {
            if (isShiftPressed()) amount *= 10;
            if (axis == 0)
            {
                CMI.setTempGaugeX(CMI.tempGaugeX() + amount);
            }
            else if (axis == 1)
            {
                CMI.setTempGaugeY(CMI.tempGaugeY() + amount);
            }
            CMI.save();
        }

        private void toggleCustomHotbar()
        {
            this.customHotbar = !this.customHotbar;
            customHotbarButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " +
                (this.customHotbar ? ON : OFF)));
            CMI.setCustomHotbar(this.customHotbar);
        }

        private void toggleIconBobbing()
        {
            this.iconBobbing = !this.iconBobbing;
            iconBobbingButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " +
                (this.iconBobbing ? ON : OFF)));
            CMI.setIconBobbing(this.iconBobbing);
        }

        private void resetTempReadoutPos()
        {
            CMI.setTempGaugeX(0);
            CMI.setTempGaugeY(0);
            CMI.save();
        }
    }
}
