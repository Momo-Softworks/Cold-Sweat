package net.momostudios.coldsweat.client.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.momostudios.coldsweat.config.ColdSweatConfig;
import net.momostudios.coldsweat.config.ConfigCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public abstract class ConfigPageBase extends Screen
{
    private final Screen parentScreen;
    private final ConfigCache configCache;

    private static final int TITLE_HEIGHT = ConfigScreen.TITLE_HEIGHT;
    private static final int BOTTOM_BUTTON_HEIGHT_OFFSET = ConfigScreen.BOTTOM_BUTTON_HEIGHT_OFFSET;
    private static final int BOTTOM_BUTTON_WIDTH = ConfigScreen.BOTTOM_BUTTON_WIDTH;
    public static Minecraft mc = Minecraft.getInstance();

    ResourceLocation divider = new ResourceLocation("cold_sweat:textures/gui/screen/configs/style_divider.png");

    ImageButton nextNavButton;
    ImageButton prevNavButton;

    public ITextComponent sectionOneTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.temperature_details");
    }

    @Nullable
    public ITextComponent sectionTwoTitle()
    {
        return new TranslationTextComponent("cold_sweat.config.section.other");
    }

    public ConfigPageBase(Screen parentScreen, ConfigCache configCache)
    {
        super(new TranslationTextComponent("cold_sweat.config.title"));
        this.parentScreen = parentScreen;
        this.configCache = configCache;
    }

    public int index()
    {
        return 0;
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

        // Navigation
        nextNavButton = new ImageButton(this.width - 32, 12, 20, 20, 0, 88, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button ->
                mc.displayGuiScreen(ConfigScreen.getPage(this.index() + 1, parentScreen, configCache)));
        if (this.index() < ConfigScreen.LAST_PAGE)
            this.addButton(nextNavButton);

        prevNavButton = new ImageButton(this.width - 76, 12, 20, 20, 20, 88, 20,
            new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button ->
                mc.displayGuiScreen(ConfigScreen.getPage(this.index() - 1, parentScreen, configCache)));
        if (this.index() > ConfigScreen.FIRST_PAGE)
            this.addButton(prevNavButton);
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);

        drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, TITLE_HEIGHT, 0xFFFFFF);

        // Page Number
        drawString(matrixStack, this.font, new StringTextComponent(this.index() + 1 + "/" + (ConfigScreen.LAST_PAGE + 1)), this.width - 53, 18, 16777215);

        // Section 1 Title
        drawString(matrixStack, this.font, this.sectionOneTitle(), this.width / 2 - 204, this.height / 4 - 28, 16777215);

        Minecraft.getInstance().getTextureManager().bindTexture(divider);
        this.blit(matrixStack, this.width / 2 - 202, this.height / 4 - 16, 0, 0, 1, 155);

        if (this.sectionTwoTitle() != null)
        {
            // Section 2 Title
            drawString(matrixStack, this.font, this.sectionTwoTitle(), this.width / 2 + 32, this.height / 4 - 28, 16777215);

            mc.getTextureManager().bindTexture(divider);
            this.blit(matrixStack, this.width / 2 + 34, this.height / 4 - 16, 0, 0, 1, 155);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public void close()
    {
        this.onClose();
        Minecraft.getInstance().displayGuiScreen(this.parentScreen);
    }
}
