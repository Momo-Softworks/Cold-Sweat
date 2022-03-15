package dev.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.util.config.ConfigScreenElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import dev.momostudios.coldsweat.config.ConfigCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class ConfigPageBase extends Screen
{
    private final Screen parentScreen;
    private final ConfigCache configCache;

    public List<ConfigScreenElement> inputBoxes = new ArrayList<>();

    private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
    private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;
    public static Minecraft mc = Minecraft.getInstance();

    ResourceLocation divider = new ResourceLocation("cold_sweat:textures/gui/screen/configs/style_divider.png");

    ImageButton nextNavButton;
    ImageButton prevNavButton;

    public BaseComponent sectionOneTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.temperature_details");
    }

    @Nullable
    public BaseComponent sectionTwoTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.other");
    }

    public ConfigPageBase(Screen parentScreen, ConfigCache configCache)
    {
        super(new TranslatableComponent("cold_sweat.config.title"));
        this.parentScreen = parentScreen;
        this.configCache = configCache;
    }

    public int index()
    {
        return 0;
    }

    public void addInput(EditBox textBox, Component component, Side side, boolean requireOP)
    {
        this.inputBoxes.add(new ConfigScreenElement(textBox, component, side, requireOP));
        textBox.x =
                side == Side.LEFT ?
                        this.font.width(component.getString()) < 98 ? this.width / 2 - (35 + textBox.getWidth()) :
                        this.width / 2 - (35 + textBox.getWidth()) + (this.font.width(component.getString()) - 94)
                : // right
                        this.font.width(component.getString()) < 98 ? this.width / 2 + 151 :
                        this.width / 2 + 151 + (this.font.width(component.getString()) - 94);
        this.addRenderableWidget(textBox);
    }

    @Override
    protected void init()
    {
        this.addRenderableWidget(new Button(
            this.width / 2 - BOTTOM_BUTTON_WIDTH / 2,
            this.height - BOTTOM_BUTTON_HEIGHT_OFFSET,
            BOTTOM_BUTTON_WIDTH, 20,
            new TranslatableComponent("gui.done"),
            button -> this.close())
        );

        // Navigation
        nextNavButton = new ImageButton(this.width - 32, 12, 20, 20, 0, 88, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button ->
                mc.setScreen(ConfigScreen.getPage(this.index() + 1, parentScreen, configCache)));
        if (this.index() < ConfigScreen.LAST_PAGE)
            this.addRenderableWidget(nextNavButton);

        prevNavButton = new ImageButton(this.width - 76, 12, 20, 20, 20, 88, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button ->
                mc.setScreen(ConfigScreen.getPage(this.index() - 1, parentScreen, configCache)));
        if (this.index() > ConfigScreen.FIRST_PAGE)
            this.addRenderableWidget(prevNavButton);
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);

        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        // Page Number
        drawString(matrixStack, this.font, new TextComponent(this.index() + 1 + "/" + (ConfigScreen.LAST_PAGE + 1)), this.width - 53, 18, 16777215);

        // Section 1 Title
        drawString(matrixStack, this.font, this.sectionOneTitle(), this.width / 2 - 204, this.height / 4 - 28, 16777215);

        RenderSystem.setShaderTexture(0, divider);
        this.blit(matrixStack, this.width / 2 - 202, this.height / 4 - 16, 0, 0, 1, 155);

        if (this.sectionTwoTitle() != null)
        {
            // Section 2 Title
            drawString(matrixStack, this.font, this.sectionTwoTitle(), this.width / 2 + 32, this.height / 4 - 28, 16777215);

            RenderSystem.setShaderTexture(0, divider);
            this.blit(matrixStack, this.width / 2 + 34, this.height / 4 - 16, 0, 0, 1, 155);
        }

        // Render labels for text boxes
        for (ConfigScreenElement inputBox : this.inputBoxes)
        {
            drawString(matrixStack, this.font, inputBox.label.getString(), inputBox.side == Side.LEFT ? this.width / 2 - 185 : this.width / 2 + 51,
                    inputBox.textBox.y + 6, inputBox.requireOP ? mc.player == null ? 16777215 : mc.player.getPermissionLevel() > 2 ? 16777215 : 8421504 : 16777215);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick()
    {
        super.tick();
        for (ConfigScreenElement inputBox : this.inputBoxes)
        {
            inputBox.textBox.tick();
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public void close()
    {
        this.onClose();
        Minecraft.getInstance().setScreen(this.parentScreen);
    }

    public enum Side
    {
        LEFT,
        RIGHT
    }
}
