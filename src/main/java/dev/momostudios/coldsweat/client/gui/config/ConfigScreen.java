package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import dev.momostudios.coldsweat.core.network.message.ClientConfigSendMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.GuiUtils;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.util.CSMath;
import dev.momostudios.coldsweat.util.Units;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ConfigScreen
{
    public static final int TITLE_HEIGHT = 16;
    public static final int BOTTOM_BUTTON_HEIGHT_OFFSET = 26;
    public static final int OPTION_SIZE = 25;
    public static final int BOTTOM_BUTTON_WIDTH = 150;

    private static final ClientSettingsConfig CLIENT_CONFIG = ClientSettingsConfig.getInstance();
    public static Minecraft mc = Minecraft.getInstance();

    static DecimalFormat twoPlaces = new DecimalFormat("#.##");

    public static boolean isMouseDown = false;
    public static int mouseX = 0;
    public static int mouseY = 0;

    public static int FIRST_PAGE = 0;
    public static int LAST_PAGE = 1;

    public static Screen getPage(int index, Screen parentScreen, ConfigCache configCache)
    {
        index = Math.max(FIRST_PAGE, Math.min(LAST_PAGE, index));
        switch (index)
        {
            case 0:  return new PageOne(parentScreen, configCache);
            case 1:  return new PageTwo(parentScreen, configCache);
            default: return null;
        }
    }

    public static void saveConfig(ConfigCache configCache)
    {
        if (Minecraft.getInstance().player != null)
        {
            if (Minecraft.getInstance().player.getPermissionLevel() >= 2)
            {
                if (!mc.isLocalServer())
                {
                    ColdSweatPacketHandler.INSTANCE.sendToServer(new ClientConfigSendMessage(configCache));
                }
                else
                {
                    ColdSweatConfig.getInstance().writeValues(configCache);
                }
            }
        }
        else
        {
            ColdSweatConfig.getInstance().writeValues(configCache);
        }
        ConfigCache.setInstance(configCache);
    }

    @SubscribeEvent
    public static void onClicked(ScreenEvent.MouseClickedEvent event)
    {
        if (event.getButton() == 0 && Minecraft.getInstance().screen instanceof DifficultyPage)
            isMouseDown = true;
    }

    @SubscribeEvent
    public static void onReleased(ScreenEvent.MouseReleasedEvent event)
    {
        if (event.getButton() == 0 && Minecraft.getInstance().screen instanceof DifficultyPage)
            isMouseDown = false;
    }

    public static String difficultyName(int difficulty)
    {
        return  difficulty == 0 ? new TranslatableComponent("cold_sweat.config.difficulty.super_easy.name").getString() :
                difficulty == 1 ? new TranslatableComponent("cold_sweat.config.difficulty.easy.name").getString() :
                difficulty == 2 ? new TranslatableComponent("cold_sweat.config.difficulty.normal.name").getString() :
                difficulty == 3 ? new TranslatableComponent("cold_sweat.config.difficulty.hard.name").getString() :
                difficulty == 4 ? new TranslatableComponent("cold_sweat.config.difficulty.custom.name").getString() : "";
    }

    public static int difficultyColor(int difficulty)
    {
        return  difficulty == 0 ? 16777215 :
                difficulty == 1 ? 16768882 :
                difficulty == 2 ? 16755024 :
                difficulty == 3 ? 16731202 :
                difficulty == 4 ? 10631158 : 16777215;
    }

    public static int textOptionColor()
    {
        return Minecraft.getInstance().player == null || Minecraft.getInstance().player.getPermissionLevel() > 2 ? 16777215 : 8421504;
    }

    static int getWidth(String translationKey, Font font)
    {
        return font.width(new TranslatableComponent(translationKey).getString());
    }

    public static class DifficultyPage extends Screen
    {
        private final Screen parentScreen;
        private final ConfigCache configCache;

        private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
        private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
        private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;

        ResourceLocation configButtons = new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png");
        ResourceLocation diffTextBox = new ResourceLocation("cold_sweat:textures/gui/screen/configs/difficulty_description.png");

        protected DifficultyPage(Screen parentScreen, ConfigCache configCache)
        {
            super(new TranslatableComponent("cold_sweat.config.section.difficulty.name"));
            this.parentScreen = parentScreen;
            this.configCache = configCache;
        }

        public int index()
        {
            return -1;
        }


        @Override
        protected void init()
        {
            this.addWidget(new Button(
                    this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
                    this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
                    BOTTOM_BUTTON_WIDTH, 20,
                    new TranslatableComponent("gui.done"),
                    button -> this.close())
            );
        }

        @Override
        public void render(@Nonnull PoseStack PoseStack, int mouseX, int mouseY, float partialTicks)
        {
            this.renderBackground(PoseStack);

            int extra = 0;
            for (String text : DifficultyDescriptions.getListFor(configCache.difficulty))
            {
                int lineWidth = font.width(text);
                if (lineWidth > extra && lineWidth > 300)
                    extra = Math.abs(lineWidth - 300) / 2;
            }

            // Draw Background
            Matrix4f ms = PoseStack.last().pose();
            int bgColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
            int borderColor = GuiUtils.DEFAULT_BORDER_COLOR_START;
            int borderColor2 = GuiUtils.DEFAULT_BORDER_COLOR_END;
            int middleX = this.width / 2;
            int middleY = this.height / 2;
            drawGradientRect(ms, 0, middleX - 169 - extra, middleY - 29, middleX + 169 + extra, middleY + 98, bgColor, bgColor); // BG

            drawGradientRect(ms, 0, middleX - 169 - extra, middleY + 98, middleX + 169 + extra, middleY + 99, bgColor, bgColor); // bottom
            drawGradientRect(ms, 0, middleX - 169 - extra, middleY - 30, middleX + 169 + extra, middleY - 29, bgColor, bgColor); // top
            drawGradientRect(ms, 0, middleX - 170 - extra, middleY - 29, middleX - 169 - extra, middleY + 98, bgColor, bgColor); // left
            drawGradientRect(ms, 0, middleX + 169 + extra, middleY - 29, middleX + 170 + extra, middleY + 98, bgColor, bgColor); // right

            drawGradientRect(ms, 0, middleX - 169 - extra, middleY + 97, middleX + 169 + extra, middleY + 98, borderColor2, borderColor2); // bottom border
            drawGradientRect(ms, 0, middleX - 169 - extra, middleY - 29, middleX + 169 + extra, middleY - 28, borderColor, borderColor); // top border
            drawGradientRect(ms, 0, middleX - 169 - extra, middleY - 28, middleX - 168 - extra, middleY + 97, borderColor, borderColor2); // left border
            drawGradientRect(ms, 0, middleX + 168 + extra, middleY - 28, middleX + 169 + extra, middleY + 97, borderColor, borderColor2); // right border

            ConfigScreen.mouseX = mouseX;
            ConfigScreen.mouseY = mouseY;

            drawCenteredString(PoseStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

            mc.getTextureManager().bindForSetup(configButtons);

            // Draw Slider
            this.blit(PoseStack, this.width / 2 - 76, this.height / 2 - 53, 12,
                    isMouseOverSlider(mouseX, mouseY) ? 174 : 168, 152, 6);

            // Draw Slider Head
            this.blit(PoseStack, this.width / 2 - 78 + (configCache.difficulty * 37), this.height / 2 - 58,
                    isMouseOverSlider(mouseX, mouseY) ? 0 : 6, 168, 6, 16);

            // Draw Difficulty Title
            String difficultyName = ConfigScreen.difficultyName(configCache.difficulty);
            this.font.drawShadow(PoseStack, difficultyName, this.width / 2.0f - (font.width(difficultyName) / 2f),
                    this.height / 2.0f - 84, ConfigScreen.difficultyColor(configCache.difficulty));

            // Draw Difficulty Description
            int line = 0;
            for (String text : DifficultyDescriptions.getListFor(configCache.difficulty))
            {
                this.font.draw(PoseStack, text, this.width / 2f - 162 - extra, this.height / 2f - 22 + (line * 20f), 15393256);
                line++;
            }
            super.render(PoseStack, mouseX, mouseY, partialTicks);
        }

        @SuppressWarnings("deprecation")
        private static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int startColor, int endColor)
        {
            float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
            float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
            float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
            float startBlue  = (float)(startColor       & 255) / 255.0F;
            float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
            float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
            float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
            float endBlue    = (float)(endColor         & 255) / 255.0F;

            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            buffer.vertex(mat, right,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,  left,    top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
            buffer.vertex(mat,  left, bottom, zLevel).color(endRed,   endGreen,   endBlue,   endAlpha).endVertex();
            buffer.vertex(mat, right, bottom, zLevel).color(endRed,   endGreen,   endBlue,   endAlpha).endVertex();

            buffer.end();
            BufferUploader.end(buffer);

            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
        }

        private void close()
        {
            // Super Easy
            if (configCache.difficulty == 0)
            {
                configCache.minTemp = CSMath.convertUnits(40, Units.F, Units.MC, true);
                configCache.maxTemp = CSMath.convertUnits(120, Units.F, Units.MC, true);
                configCache.rate = 0.5;
                configCache.showAmbient = false;
                configCache.damageScaling = false;
                configCache.fireRes = true;
                configCache.iceRes = true;
            }
            // Easy
            else if (configCache.difficulty == 1)
            {
                configCache.minTemp = CSMath.convertUnits(45, Units.F, Units.MC, true);
                configCache.maxTemp = CSMath.convertUnits(110, Units.F, Units.MC, true);
                configCache.rate = 0.75;
                configCache.showAmbient = false;
                configCache.damageScaling = false;
                configCache.fireRes = true;
                configCache.iceRes = true;
            }
            // Normal
            else if (configCache.difficulty == 2)
            {
                configCache.minTemp = CSMath.convertUnits(50, Units.F, Units.MC, true);
                configCache.maxTemp = CSMath.convertUnits(100, Units.F, Units.MC, true);
                configCache.rate = 1.0;
                configCache.showAmbient = true;
                configCache.damageScaling = true;
                configCache.fireRes = false;
                configCache.iceRes = false;
            }
            // Hard
            else if (configCache.difficulty == 3)
            {
                configCache.minTemp = CSMath.convertUnits(60, Units.F, Units.MC, true);
                configCache.maxTemp = CSMath.convertUnits(90, Units.F, Units.MC, true);
                configCache.rate = 1.5;
                configCache.showAmbient = true;
                configCache.damageScaling = true;
                configCache.fireRes = false;
                configCache.iceRes = false;
            }
            mc.setScreen(parentScreen);
            saveConfig(configCache);
        }

        boolean isMouseOverSlider(double mouseX, double mouseY)
        {
            return (mouseX >= this.width / 2.0 - 80 && mouseX <= this.width / 2.0 + 80 &&
                    mouseY >= this.height / 2.0 - 67 && mouseY <= this.height / 2.0 - 35);
        }

       @Override
       public void tick()
       {
           double x = mouseX;
           double y = mouseY;
           if (isMouseDown && isMouseOverSlider(x, y))
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

               if (newDifficulty != configCache.difficulty) {
                   mc.getSoundManager().play(SimpleSoundInstance.forUI(new SoundEvent(new ResourceLocation("minecraft:block.note_block.hat")), 1.8f, 0.5f));
               }
               configCache.difficulty = newDifficulty;
           }
       }
    }

    public static class PageOne extends ConfigPageBase
    {
        Screen parentScreen;
        ConfigCache configCache;
        private final String ON;
        private final String OFF;

        boolean celsius = CLIENT_CONFIG.celsius();

        EditBox tempOffsetInput;
        EditBox maxTempInput;
        EditBox minTempInput;
        EditBox rateMultInput;
        Button difficultyButton;
        Button celsiusButton;
        Button iceResButton;
        Button fireResButton;
        Button damageScalingButton;
        Button showAmbientButton;

        public PageOne(Screen parentScreen, ConfigCache configCache)
        {
            super(parentScreen, configCache);
            this.parentScreen = parentScreen;
            this.configCache = configCache;
            ON = new TranslatableComponent("options.on").getString();
            OFF = new TranslatableComponent("options.off").getString();
        }

        @Override
        public int index()
        {
            return 0;
        }

        @Override
        public BaseComponent sectionTwoTitle()
        {
            return new TranslatableComponent("cold_sweat.config.section.difficulty.name");
        }

        @Override
        protected void init()
        {
            super.init();

            // The options

            // Celsius
            celsiusButton = new ConfigButton(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                (this.celsius ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())), button -> this.toggleCelsius(), configCache)
            {
                @Override
                public boolean setsCustomDifficulty() { return false; }
            };
            celsiusButton.setWidth(Math.max(152, font.width(celsiusButton.getMessage().getString())));


            // Temp Offset
            int offsetBoxX = getWidth("cold_sweat.config.temp_offset.name", font) < 98 ?
            this.width / 2 - 86 : this.width / 2 - 183 + getWidth("cold_sweat.config.temp_offset.name", font);

            this.tempOffsetInput = new EditBox(font, offsetBoxX, this.height / 4 + 20, 51, 22, new TextComponent(""));
            this.tempOffsetInput.setValue(String.valueOf(CLIENT_CONFIG.tempOffset()));

            // Max Temperature
            int maxBoxX = getWidth("cold_sweat.config.max_temperature.name", font) < 98 ?
            this.width / 2 - 86 : this.width / 2 - 183 + getWidth("cold_sweat.config.max_temperature.name", font);

            this.maxTempInput = new EditBox(font, maxBoxX, this.height / 4 + 52, 51, 22, new TextComponent(""));
            this.maxTempInput.setValue(String.valueOf(twoPlaces.format(
                    CSMath.convertUnits(configCache.maxTemp, Units.MC, celsius ? Units.C : Units.F, true))));

            // Min Temperature
            int minBoxX = getWidth("cold_sweat.config.min_temperature.name", font) < 98 ?
            this.width / 2 - 86 : this.width / 2 - 183 + getWidth("cold_sweat.config.min_temperature.name", font);

            this.minTempInput = new EditBox(font, minBoxX, this.height / 4 + 84, 51, 22, new TextComponent(""));
            this.minTempInput.setValue(String.valueOf(twoPlaces.format(
                    CSMath.convertUnits(configCache.minTemp, Units.MC, celsius ? Units.C : Units.F, true))));

            // Rate Multiplier
            int rateBoxX = getWidth("cold_sweat.config.rate_multiplier.name", font) < 98 ?
            this.width / 2 - 86 : this.width / 2 - 183 + getWidth("cold_sweat.config.rate_multiplier.name", font);

            this.rateMultInput = new EditBox(font, rateBoxX, this.height / 4 + 116, 51, 22, new TextComponent(""));
            this.rateMultInput.setValue(String.valueOf(configCache.rate));

            // Difficulty button
            difficultyButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8, 152, 20,
                    new TextComponent(new TranslatableComponent("cold_sweat.config.difficulty.name").getString() +
                    " (" + difficultyName(configCache.difficulty) + ")..."),
                    button -> mc.setScreen(new DifficultyPage(this, configCache)), configCache)
            {
                @Override
                public boolean setsCustomDifficulty() { return false; }
            };
            difficultyButton.setWidth(Math.max(152, font.width(difficultyButton.getMessage().getString()) + 4));


            // Misc. Temp Effects
            iceResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 2, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " + (configCache.iceRes ? ON : OFF)),
                button -> this.toggleIceRes(), configCache);
            iceResButton.setWidth(Math.max(152, font.width(iceResButton.getMessage().getString()) + 4));

            fireResButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " + (configCache.fireRes ? ON : OFF)),
                button -> this.toggleFireRes(), configCache);
            fireResButton.setWidth(Math.max(152, font.width(fireResButton.getMessage().getString()) + 4));

            showAmbientButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " + (configCache.showAmbient ? ON : OFF)),
                button -> this.toggleShowAmbient(), configCache);
            showAmbientButton.setWidth(Math.max(152, font.width(showAmbientButton.getMessage().getString()) + 4));

            damageScalingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 5, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " + (configCache.damageScaling ? ON : OFF)),
                button -> this.toggleDamageScaling(), configCache);
            damageScalingButton.setWidth(Math.max(152, font.width(damageScalingButton.getMessage().getString()) + 4));

            this.addWidget(difficultyButton);

            this.addWidget(iceResButton);
            this.addWidget(fireResButton);
            this.addWidget(showAmbientButton);
            this.addWidget(damageScalingButton);

            if (mc.player != null && mc.player.getPermissionLevel() < 2)
            {
                difficultyButton.active = false;
                iceResButton.active = false;
                fireResButton.active = false;
                showAmbientButton.active = false;
                damageScalingButton.active = false;
            }
            else
            {
                this.addWidget(this.maxTempInput);
                this.addWidget(this.minTempInput);
                this.addWidget(this.rateMultInput);
            }

            this.addWidget(celsiusButton);
            this.addWidget(this.tempOffsetInput);
        }

        @Override
        public void render(@Nonnull PoseStack PoseStack, int mouseX, int mouseY, float partialTicks)
        {
            super.render(PoseStack, mouseX, mouseY, partialTicks);

            /*
             * Render config options
             */

            // Temp Offset
            this.tempOffsetInput.render(PoseStack, mouseX, mouseY, partialTicks);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.temp_offset.name"), this.width / 2 - 185, tempOffsetInput.y + 6, 16777215);

            // Max Temp
            this.maxTempInput.render(PoseStack, mouseX, mouseY, partialTicks);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.max_temperature.name"), this.width / 2 - 185, maxTempInput.y + 6, textOptionColor());

            // Min Temp
            this.minTempInput.render(PoseStack, mouseX, mouseY, partialTicks);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.min_temperature.name"), this.width / 2 - 185, minTempInput.y + 6, textOptionColor());

            // Rate Multiplier
            this.rateMultInput.render(PoseStack, mouseX, mouseY, partialTicks);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.rate_multiplier.name"), this.width / 2 - 185, rateMultInput.y + 6, textOptionColor());
        }

        @Override
        public void tick()
        {
            super.tick();
            tempOffsetInput.tick();
            maxTempInput.tick();
            minTempInput.tick();
            rateMultInput.tick();
            if (mc.player != null && mc.player.getPermissionLevel() < 2)
            {
                maxTempInput.setValue(String.valueOf(twoPlaces.format(CSMath.convertUnits(configCache.maxTemp, Units.MC, celsius ? Units.C : Units.F, true))));
                minTempInput.setValue(String.valueOf(twoPlaces.format(CSMath.convertUnits(configCache.minTemp, Units.MC, celsius ? Units.C : Units.F, true))));
                rateMultInput.setValue(String.valueOf(configCache.rate));
            }
        }

        private void save()
        {
            CLIENT_CONFIG.setCelsius(this.celsius);

            try
            {
                CLIENT_CONFIG.setTempOffset(Integer.parseInt(tempOffsetInput.getValue()));
            } catch (Exception e) {}

            try
            {
                configCache.maxTemp = CSMath.convertUnits(Double.parseDouble(maxTempInput.getValue()), celsius ? Units.C : Units.F, Units.MC, true);
            } catch (Exception e) {}

            try
            {
                configCache.minTemp = CSMath.convertUnits(Double.parseDouble(minTempInput.getValue()), celsius ? Units.C : Units.F, Units.MC, true);
            } catch (Exception e) {}

            try
            {
                double rateModifier = Double.parseDouble(rateMultInput.getValue());
                configCache.rate = rateModifier;
            } catch (Exception e) {}

            saveConfig(configCache);

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
            celsiusButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.units.name").getString() + ": " +
                (this.celsius ? new TranslatableComponent("cold_sweat.config.celsius.name").getString() :
                                new TranslatableComponent("cold_sweat.config.fahrenheit.name").getString())));

            minTempInput.setValue(String.valueOf(twoPlaces.format(CSMath.convertUnits(configCache.minTemp, Units.MC, celsius ? Units.C : Units.F, true))));
            maxTempInput.setValue(String.valueOf(twoPlaces.format(CSMath.convertUnits(configCache.maxTemp, Units.MC, celsius ? Units.C : Units.F, true))));
        }

        public void toggleIceRes()
        {
            configCache.iceRes = !configCache.iceRes;
            iceResButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.ice_resistance.name").getString() + ": " +
                (configCache.iceRes ? ON : OFF)));
        }

        public void toggleFireRes()
        {
            configCache.fireRes = !configCache.fireRes;
            fireResButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.fire_resistance.name").getString() + ": " +
                (configCache.fireRes ? ON : OFF)));
        }
        public void toggleDamageScaling()
        {
            configCache.damageScaling = !configCache.damageScaling;
            damageScalingButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.damage_scaling.name").getString() + ": " +
                (configCache.damageScaling ? ON : OFF)));
        }

        public void toggleShowAmbient()
        {
            configCache.showAmbient = !configCache.showAmbient;
            showAmbientButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.require_thermometer.name").getString() + ": " +
                (configCache.showAmbient ? ON : OFF)));
        }
    }

    public static class PageTwo extends ConfigPageBase
    {
        private final Screen parentScreen;
        private final ConfigCache configCache;
        private final String ON;
        private final String OFF;

        boolean customHotbar = CLIENT_CONFIG.customHotbar();
        boolean iconBobbing = CLIENT_CONFIG.iconBobbing();

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

        boolean gracePeriod;
        int gracePeriodLength;
        Button gracePeriodButton;
        EditBox gracePeriodLengthInput;


        public PageTwo(Screen parentScreen, ConfigCache configCache)
        {
            super(parentScreen, configCache);
            this.parentScreen = parentScreen;
            this.configCache = configCache;
            gracePeriod = configCache.gracePeriodEnabled;
            gracePeriodLength = configCache.gracePeriodLength;
            ON = new TranslatableComponent("options.on").getString();
            OFF = new TranslatableComponent("options.off").getString();
        }

        @Override
        public int index()
        {
            return 1;
        }

        @Override
        public BaseComponent sectionOneTitle() {
            return new TranslatableComponent("cold_sweat.config.section.other");
        }

        @Nullable
        @Override
        public BaseComponent sectionTwoTitle() {
            return new TranslatableComponent("cold_sweat.config.section.hud_settings");
        }

        @Override
        protected void init()
        {
            super.init();

            // The options

            // Enable Grace Period
            gracePeriodButton = new ConfigButton(this.width / 2 - 185, this.height / 4 - 8, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": "
                + (configCache.gracePeriodEnabled ? ON : OFF)), button -> this.toggleGracePeriod(), configCache);
            gracePeriodButton.setWidth(Math.max(152, font.width(gracePeriodButton.getMessage().getString()) + 4));

            // Grace Period Length
            this.gracePeriodLengthInput = new EditBox(font, this.width / 2 - 86, this.height / 4 + 20, 51, 22, new TextComponent(""));
            this.gracePeriodLengthInput.setValue(configCache.gracePeriodLength + "");

            // Direction Buttons: Steve Head
            int steveOffs = getWidth("cold_sweat.config.temperature_icon.name", font) > 84 ?
                    getWidth("cold_sweat.config.temperature_icon.name", font) - 84 : 0;

            leftSteveButton = new ImageButton(this.width / 2 + 140 + steveOffs, this.height / 4 - 8, 14, 20, 0, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(0, -1));
            upSteveButton = new ImageButton(this.width / 2 + 154 + steveOffs, this.height / 4 - 8, 20, 10, 14, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(1, -1));
            downSteveButton = new ImageButton(this.width / 2 + 154 + steveOffs, this.height / 4 + 2, 20, 10, 14, 10, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(1, 1));
            rightSteveButton = new ImageButton(this.width / 2 + 174 + steveOffs, this.height / 4 - 8, 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeSelfIndicatorPos(0, 1));
            resetSteveButton = new ImageButton(this.width / 2 + 192 + steveOffs, this.height / 4 - 8, 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetSelfIndicatorPos());

            // Direction Buttons: Temp Readout
            int tempOffs = getWidth("cold_sweat.config.temperature_readout.name", font) > 84 ?
                    getWidth("cold_sweat.config.temperature_readout.name", font) - 84 : 0;

            leftTempReadoutButton = new ImageButton(this.width / 2 + 140 + tempOffs, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 14, 20, 0, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, -1));
            upTempReadoutButton = new ImageButton(this.width / 2 + 154 + tempOffs, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, -1));
            downTempReadoutButton = new ImageButton(this.width / 2 + 154 + tempOffs, this.height / 4 + 2 + (int) (OPTION_SIZE * 1.5), 20, 10, 14, 10, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, 1));
            rightTempReadoutButton = new ImageButton(this.width / 2 + 174 + tempOffs, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, 1));
            resetTempReadoutButton = new ImageButton(this.width / 2 + 192 + tempOffs, this.height / 4 - 8 + (int) (OPTION_SIZE * 1.5), 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetTempReadoutPos());

            // Custom Hotbar
            customHotbarButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 3, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": "
                + (this.customHotbar ? ON : OFF)), button -> this.toggleCustomHotbar(), configCache);
            customHotbarButton.setWidth(Math.max(152, font.width(customHotbarButton.getMessage().getString()) + 4));

            // Icon Bobbing
            iconBobbingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + OPTION_SIZE * 4, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString()
                + ": " + (this.iconBobbing ? ON : OFF)), button -> this.toggleIconBobbing(), configCache);
            iconBobbingButton.setWidth(Math.max(152, font.width(iconBobbingButton.getMessage().getString()) + 4));


            this.addWidget(upSteveButton);
            this.addWidget(downSteveButton);
            this.addWidget(leftSteveButton);
            this.addWidget(rightSteveButton);
            this.addWidget(resetSteveButton);

            this.addWidget(upTempReadoutButton);
            this.addWidget(downTempReadoutButton);
            this.addWidget(leftTempReadoutButton);
            this.addWidget(rightTempReadoutButton);
            this.addWidget(resetTempReadoutButton);

            this.addWidget(iconBobbingButton);
            this.addWidget(customHotbarButton);
            this.addWidget(gracePeriodButton);

            if (mc.player == null || mc.player.getPermissionLevel() >= 2)
            {
                this.addWidget(gracePeriodLengthInput);
            }
            else
            {
                gracePeriodButton.active = false;
            }
        }

        @Override
        public void tick()
        {
            super.tick();
            gracePeriodLengthInput.tick();
        }

        @Override
        public void render(@Nonnull PoseStack PoseStack, int mouseX, int mouseY, float partialTicks)
        {
            super.render(PoseStack, mouseX, mouseY, partialTicks);

            gracePeriodLengthInput.render(PoseStack, mouseX, mouseY, partialTicks);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.grace_period_length.name"), this.width / 2 - 185,
                    gracePeriodLengthInput.y + 6, textOptionColor());

            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.temperature_icon.name"), this.width / 2 + 51,
                    this.height / 4 - 2, 16777215);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.temperature_readout.name"), this.width / 2 + 51,
                    this.height / 4 - 2 + (int) (OPTION_SIZE * 1.5), 16777215);
            drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.offset_shift.name"), this.width / 2 + 51,
                    this.height / 4 + 128, 16777215);
        }

        @Override
        public void onClose()
        {
            super.onClose();
            configCache.gracePeriodLength = Integer.parseInt(gracePeriodLengthInput.getValue());
            configCache.gracePeriodEnabled = gracePeriod;
            saveConfig(configCache);
        }

        private void changeSelfIndicatorPos(int axis, int amount)
        {
            if (Screen.hasShiftDown()) amount *= 10;
            if (axis == 0)
            {
                CLIENT_CONFIG.setSteveHeadX(CLIENT_CONFIG.steveHeadX() + amount);
            }
            else if (axis == 1)
            {
                CLIENT_CONFIG.setSteveHeadY(CLIENT_CONFIG.steveHeadY() + amount);
            }
        }

        private void resetSelfIndicatorPos()
        {
            CLIENT_CONFIG.setSteveHeadX(0);
            CLIENT_CONFIG.setSteveHeadY(0);
        }

        private void changeTempReadoutPos(int axis, int amount)
        {
            if (Screen.hasShiftDown()) amount *= 10;
            if (axis == 0)
            {
                CLIENT_CONFIG.setTempGaugeX(CLIENT_CONFIG.tempGaugeX() + amount);
            }
            else if (axis == 1)
            {
                CLIENT_CONFIG.setTempGaugeY(CLIENT_CONFIG.tempGaugeY() + amount);
            }
        }

        private void resetTempReadoutPos()
        {
            CLIENT_CONFIG.setTempGaugeX(0);
            CLIENT_CONFIG.setTempGaugeY(0);
        }

        private void toggleCustomHotbar()
        {
            this.customHotbar = !this.customHotbar;
            customHotbarButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " +
                (this.customHotbar ? ON : OFF)));
            CLIENT_CONFIG.setCustomHotbar(this.customHotbar);
            RearrangeHotbar.customHotbar = this.customHotbar;
        }

        private void toggleIconBobbing()
        {
            this.iconBobbing = !this.iconBobbing;
            iconBobbingButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " +
                (this.iconBobbing ? ON : OFF)));
            CLIENT_CONFIG.setIconBobbing(this.iconBobbing);
        }

        public void toggleGracePeriod()
        {
            gracePeriod = !gracePeriod;
            gracePeriodButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": " +
                    (gracePeriod ? ON : OFF)));
        }
    }
}
