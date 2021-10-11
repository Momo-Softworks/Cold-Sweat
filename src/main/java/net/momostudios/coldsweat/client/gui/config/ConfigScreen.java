package net.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.java.games.input.Mouse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.core.util.MathHelperCS;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ConfigScreen
{
    public static final int TITLE_HEIGHT = 16;
    public static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;
    public static final int OPTION_SIZE = 25;
    public static final int BOTTOM_BUTTON_WIDTH = 150;
    private static final String ON = new TranslationTextComponent("options.on").getString();
    private static final String OFF = new TranslationTextComponent("options.off").getString();

    private static final ColdSweatConfig CMI = ColdSweatConfig.getInstance();
    public static final ConfigScreen INSTANCE = new ConfigScreen();
    public Screen parentScreen = new OptionsScreen(new IngameMenuScreen(true), Minecraft.getInstance().gameSettings);
    public static Minecraft mc = Minecraft.getInstance();

    protected int difficulty = CMI.difficulty() - 1;

    public boolean isMouseDown = false;
    public int mouseX = 0;
    public int mouseY = 0;

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

    @SubscribeEvent
    public static void onClicked(GuiScreenEvent.MouseClickedEvent event)
    {
        if (event.getButton() == 0 && Minecraft.getInstance().currentScreen instanceof DifficultyPage)
            INSTANCE.isMouseDown = true;
    }

    @SubscribeEvent
    public static void onReleased(GuiScreenEvent.MouseReleasedEvent event)
    {
        if (Minecraft.getInstance().currentScreen instanceof DifficultyPage)
            INSTANCE.isMouseDown = false;
    }

    public String difficultyName()
    {
        return  INSTANCE.difficulty == 0 ? new TranslationTextComponent("cold_sweat.config.difficulty.super_easy.name").getString() :
                INSTANCE.difficulty == 1 ? new TranslationTextComponent("cold_sweat.config.difficulty.easy.name").getString() :
                INSTANCE.difficulty == 2 ? new TranslationTextComponent("cold_sweat.config.difficulty.normal.name").getString() :
                INSTANCE.difficulty == 3 ? new TranslationTextComponent("cold_sweat.config.difficulty.hard.name").getString() :
                INSTANCE.difficulty == 4 ? new TranslationTextComponent("cold_sweat.config.difficulty.custom.name").getString() : "";
    }

    public int difficultyColor()
    {
        return  INSTANCE.difficulty == 0 ? 16777215 :
                INSTANCE.difficulty == 1 ? 16768882 :
                INSTANCE.difficulty == 2 ? 16755024 :
                INSTANCE.difficulty == 3 ? 16731202 :
                INSTANCE.difficulty == 4 ? 10631158 : 16777215;
    }

    public static class DifficultyPage extends Screen
    {
        private final Screen parentScreen;
        private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
        private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
        private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;
        private static final ColdSweatConfig CMI = ColdSweatConfig.getInstance();

        ResourceLocation configButtons = new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png");
        ResourceLocation diffTextBox = new ResourceLocation("cold_sweat:textures/gui/screen/configs/difficulty_description.png");

        protected DifficultyPage(Screen parentScreen) {
            super(new TranslationTextComponent("cold_sweat.config.section.difficulty.name"));
            this.parentScreen = parentScreen;
        }

        public int index()
        {
            return -1;
        }


        @Override
        protected void init()
        {
            this.addButton(new Button(
                    this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
                    this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
                    BOTTOM_BUTTON_WIDTH, 20,
                    new TranslationTextComponent("gui.done"),
                    button -> this.close())
            );
        }

        @Override
        public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
        {
            INSTANCE.mouseX = mouseX;
            INSTANCE.mouseY = mouseY;
            this.renderBackground(matrixStack);

            drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

            mc.getTextureManager().bindTexture(configButtons);
            // Slider
            this.blit(matrixStack, this.width / 2 - 76, this.height / 2 - 53, 12,
                    isMouseOverSlider(mouseX, mouseY) ? 174 : 168, 152, 6);
            // Head
            this.blit(matrixStack, this.width / 2 - 78 + (INSTANCE.difficulty * 37), this.height / 2 - 58,
                    isMouseOverSlider(mouseX, mouseY) ? 0 : 6, 168, 6, 16);
            // Difficulty Text
            this.font.drawStringWithShadow(matrixStack, INSTANCE.difficultyName(), this.width / 2.0f - (font.getStringWidth(INSTANCE.difficultyName()) / 2f),
                    this.height / 2.0f - 84, INSTANCE.difficultyColor());

            mc.getTextureManager().bindTexture(diffTextBox);
            this.blit(matrixStack, this.width / 2 - 160, this.height / 2 - 30, 0, 0, 320, 128, 320, 128);

            int line = 0;
            for (ITextComponent text : DifficultyDescriptions.getListFor(INSTANCE.difficulty))
            {
                this.font.drawString(matrixStack, text.getString(), this.width / 2f - 152, this.height / 2f - 22 + (line * 20f), 15393256);
                line++;
            }

            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        private void close()
        {
            CMI.setDifficulty(INSTANCE.difficulty);

            if (INSTANCE.difficulty == 0)
            {
                CMI.setMinHabitable(MathHelperCS.convertFromF(40));
                CMI.setMaxHabitable(MathHelperCS.convertFromF(120));
                CMI.setRateMultiplier(0.5);
                CMI.setRequireThermometer(false);
                CMI.setDamageScaling(false);
                CMI.setFireResistanceEffect(true);
                CMI.setIceResistanceEffect(true);
                CMI.setDifficulty(1);
            }
            else if (INSTANCE.difficulty == 1)
            {
                CMI.setMinHabitable(MathHelperCS.convertFromF(45));
                CMI.setMaxHabitable(MathHelperCS.convertFromF(115));
                CMI.setRateMultiplier(0.75);
                CMI.setRequireThermometer(false);
                CMI.setDamageScaling(false);
                CMI.setFireResistanceEffect(true);
                CMI.setIceResistanceEffect(true);
                CMI.setDifficulty(2);
            }
            else if (INSTANCE.difficulty == 2)
            {
                CMI.setMinHabitable(MathHelperCS.convertFromF(50));
                CMI.setMaxHabitable(MathHelperCS.convertFromF(110));
                CMI.setRateMultiplier(1.0);
                CMI.setRequireThermometer(true);
                CMI.setDamageScaling(true);
                CMI.setFireResistanceEffect(false);
                CMI.setIceResistanceEffect(false);
                CMI.setDifficulty(3);
            }
            else if (INSTANCE.difficulty == 3)
            {
                CMI.setMinHabitable(MathHelperCS.convertFromF(60));
                CMI.setMaxHabitable(MathHelperCS.convertFromF(100));
                CMI.setRateMultiplier(1.5);
                CMI.setRequireThermometer(true);
                CMI.setDamageScaling(true);
                CMI.setFireResistanceEffect(false);
                CMI.setIceResistanceEffect(false);
                CMI.setDifficulty(4);
            }
            CMI.save();
            mc.displayGuiScreen(new PageOne());
        }

        boolean isMouseOverSlider(double mouseX, double mouseY)
        {
            return (mouseX >= this.width / 2.0 - 80 && mouseX <= this.width / 2.0 + 80 &&
                    mouseY >= this.height / 2.0 - 67 && mouseY <= this.height / 2.0 - 35);
        }

       @Override
       public void tick()
       {
           double x = INSTANCE.mouseX;
           double y = INSTANCE.mouseY;
           if (INSTANCE.isMouseDown && isMouseOverSlider(x, y))
           {
               int newDifficulty = 0;
               if (x < this.width / 2.0 - 76 + (19)) {
                   newDifficulty = 0;
               }
               else if (x < this.width / 2.0 - 76 + (19 * 3)) {
                   newDifficulty = 1;
               }
               else if (x < this.width / 2.0 - 76 + (19 * 5)) {
                   newDifficulty = 2;
               }
               else if (x < this.width / 2.0 - 76 + (19 * 7)) {
                   newDifficulty = 3;
               }
               else if (x < this.width / 2.0 - 76 + (19 * 9)) {
                   newDifficulty = 4;
               }

               if (newDifficulty != INSTANCE.difficulty) {
                   mc.getSoundHandler().play(SimpleSound.master(new SoundEvent(new ResourceLocation("minecraft:block.note_block.hat")), 2f, 1f));
               }
               INSTANCE.difficulty = newDifficulty;
           }
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
        double minTemp = celsius ? MathHelperCS.convertToC(CMI.minHabitable()) : MathHelperCS.convertToF(CMI.minHabitable());
        double maxTemp = celsius ? MathHelperCS.convertToC(CMI.maxHabitable()) : MathHelperCS.convertToF(CMI.maxHabitable());

        TextFieldWidget tempOffsetInput;
        TextFieldWidget maxTempInput;
        TextFieldWidget minTempInput;
        TextFieldWidget rateMultInput;
        Button difficultyButton;
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
            celsiusButton = new ConfigButton(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.units.name").getString() + ": " +
                        (this.celsius ? new TranslationTextComponent("cold_sweat.config.celsius.name").getString() :
                                        new TranslationTextComponent("cold_sweat.config.fahrenheit.name").getString())), button -> this.toggleCelsius());
            this.addButton(celsiusButton);

            // Temp Offset
            this.tempOffsetInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 20, 51, 22, new StringTextComponent(""));
            this.tempOffsetInput.setText(String.valueOf(CMI.tempOffset()));

            // Max Temperature
            this.maxTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 52, 51, 22, new StringTextComponent(""));
            this.maxTempInput.setText(String.valueOf((int)
                    (celsius ? MathHelperCS.convertToC(CMI.maxHabitable()) : MathHelperCS.convertToF(CMI.maxHabitable()))));

            // Min Temperature
            this.minTempInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 84, 51, 22, new StringTextComponent(""));
            this.minTempInput.setText(String.valueOf((int)
                    (celsius ? MathHelperCS.convertToC(CMI.minHabitable()) : MathHelperCS.convertToF(CMI.minHabitable()))));

            // Rate Multiplier
            this.rateMultInput = new TextFieldWidget(font, this.width / 2 - 86, this.height / 4 + 116, 51, 22, new StringTextComponent(""));
            this.rateMultInput.setText(String.valueOf(CMI.rateMultiplier()));

            // Difficulty button
            difficultyButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8, 152, 20,
                    new StringTextComponent(new TranslationTextComponent("cold_sweat.config.difficulty.name").getString() +
                    " (" + INSTANCE.difficultyName() + ")..."),
                    button -> mc.displayGuiScreen(new DifficultyPage(new PageOne())))
            {
                @Override
                public boolean setsCustomDifficulty() { return false; }
            };

            this.addButton(difficultyButton);

            // Misc. Temp Effects
            iceResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 2, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (this.iceRes ? ON : OFF)),
                button -> this.toggleIceRes());
            this.addButton(iceResButton);

            fireResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (this.fireRes ? ON : OFF)),
                button -> this.toggleFireRes());
            this.addButton(fireResButton);

            requireThermometerButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (this.requireThermometer ? ON : OFF)),
                button -> this.toggleRequireThermometer());
            this.addButton(requireThermometerButton);

            /*animalTempButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.animal_temperature.name").getString() + ": " + (this.animalTemp ? ON : OFF)),
                button -> this.toggleAnimalTemp());
            this.addButton(animalTempButton);*/

            damageScalingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 5, 152, 20,
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

        private void save()
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
                CMI.setMaxHabitable(celsius ? MathHelperCS.convertFromC(Double.parseDouble(maxTempInput.getText())) :
                        MathHelperCS.convertFromF(Double.parseDouble(maxTempInput.getText())));
            } catch (Exception e) {}

            try
            {
                CMI.setMinHabitable(celsius ? MathHelperCS.convertFromC(Double.parseDouble(minTempInput.getText())) :
                        MathHelperCS.convertFromF(Double.parseDouble(minTempInput.getText())));
            } catch (Exception e) {}

            try
            {
                double rateModifier = Double.parseDouble(rateMultInput.getText());
                CMI.setRateMultiplier(rateModifier);
            } catch (Exception e) {}

        }

        @Override
        public void onClose()
        {
            save();
            super.onClose();
        }

        public void toggleCelsius()
        {
            this.celsius = !this.celsius;
            celsiusButton.setMessage(new StringTextComponent(new TranslationTextComponent("cold_sweat.config.units.name").getString() + ": " +
                (this.celsius ? new TranslationTextComponent("cold_sweat.config.celsius.name").getString() :
                                new TranslationTextComponent("cold_sweat.config.fahrenheit.name").getString())));

            minTemp = celsius ? MathHelperCS.FtoC(minTemp) : MathHelperCS.CtoF(minTemp);
            maxTemp = celsius ? MathHelperCS.FtoC(maxTemp) : MathHelperCS.CtoF(maxTemp);

            minTempInput.setText(String.valueOf((int) minTemp));
            maxTempInput.setText(String.valueOf((int) maxTemp));
            INSTANCE.difficulty = 4;
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
            upSteveButton = new ImageButton(this.width / 2 + 154, this.height / 4 - 8, 20, 10, 14, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(1, -1));
            this.addButton(upSteveButton);
            downSteveButton = new ImageButton(this.width / 2 + 154, this.height / 4 + 2, 20, 10, 14, 10, 20,
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
            upTempReadoutButton = new ImageButton(this.width / 2 + 154, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, -1));
            this.addButton(upTempReadoutButton);
            downTempReadoutButton = new ImageButton(this.width / 2 + 154, this.height / 4 + 2 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 10, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, 1));
            this.addButton(downTempReadoutButton);
            rightTempReadoutButton = new ImageButton(this.width / 2 + 174, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, 1));
            this.addButton(rightTempReadoutButton);
            resetTempReadoutButton = new ImageButton(this.width / 2 + 192, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetTempReadoutPos());
            this.addButton(resetTempReadoutButton);

            // Custom Hotbar
            customHotbarButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new StringTextComponent(new TranslationTextComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " + (this.customHotbar ? ON : OFF)),
                button -> this.toggleCustomHotbar());
            this.addButton(customHotbarButton);

            // Icon Bobbing
            iconBobbingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
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
