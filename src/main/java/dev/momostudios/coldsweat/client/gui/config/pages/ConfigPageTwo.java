package dev.momostudios.coldsweat.client.gui.config.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.momostudios.coldsweat.client.event.RearrangeHotbar;
import dev.momostudios.coldsweat.client.gui.config.ConfigButton;
import dev.momostudios.coldsweat.client.gui.config.ConfigPageBase;
import dev.momostudios.coldsweat.client.gui.config.ConfigScreen;
import dev.momostudios.coldsweat.config.ClientSettingsConfig;
import dev.momostudios.coldsweat.config.ConfigCache;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConfigPageTwo extends ConfigPageBase
{
    private final ConfigCache configCache;
    private final String ON;
    private final String OFF;

    boolean customHotbar = ClientSettingsConfig.getInstance().customHotbar();
    boolean iconBobbing = ClientSettingsConfig.getInstance().iconBobbing();

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


    public ConfigPageTwo(Screen parentScreen, ConfigCache configCache)
    {
        super(parentScreen, configCache);
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
    public BaseComponent sectionOneTitle()
    {
        return new TranslatableComponent("cold_sweat.config.section.other");
    }

    @Nullable
    @Override
    public BaseComponent sectionTwoTitle()
    {
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
        int steveOffs = font.width(new TranslatableComponent("cold_sweat.config.temperature_icon.name").getString()) > 84 ?
                        font.width(new TranslatableComponent("cold_sweat.config.temperature_icon.name").getString()) - 84 : 0;

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
        int tempOffs = font.width(new TranslatableComponent("cold_sweat.config.temperature_readout.name").getString()) > 84 ?
                       font.width(new TranslatableComponent("cold_sweat.config.temperature_readout.name").getString()) - 84 : 0;

        leftTempReadoutButton = new ImageButton(this.width / 2 + 140 + tempOffs, this.height / 4 - 8 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 14, 20, 0, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, -1));
        upTempReadoutButton = new ImageButton(this.width / 2 + 154 + tempOffs, this.height / 4 - 8 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 20, 10, 14, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, -1));
        downTempReadoutButton = new ImageButton(this.width / 2 + 154 + tempOffs, this.height / 4 + 2 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 20, 10, 14, 10, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(1, 1));
        rightTempReadoutButton = new ImageButton(this.width / 2 + 174 + tempOffs, this.height / 4 - 8 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 14, 20, 34, 0, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> changeTempReadoutPos(0, 1));
        resetTempReadoutButton = new ImageButton(this.width / 2 + 192 + tempOffs, this.height / 4 - 8 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 20, 20, 0, 128, 20,
                new ResourceLocation("cold_sweat:textures/gui/screen/configs/config_buttons.png"), button -> resetTempReadoutPos());

        // Custom Hotbar
        customHotbarButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 3, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": "
                        + (this.customHotbar ? ON : OFF)), button -> this.toggleCustomHotbar(), configCache);
        customHotbarButton.setWidth(Math.max(152, font.width(customHotbarButton.getMessage().getString()) + 4));

        // Icon Bobbing
        iconBobbingButton = new ConfigButton(this.width / 2 + 51, this.height / 4 - 8 + ConfigScreen.OPTION_SIZE * 4, 152, 20,
                new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString()
                        + ": " + (this.iconBobbing ? ON : OFF)), button -> this.toggleIconBobbing(), configCache);
        iconBobbingButton.setWidth(Math.max(152, font.width(iconBobbingButton.getMessage().getString()) + 4));


        this.addRenderableWidget(upSteveButton);
        this.addRenderableWidget(downSteveButton);
        this.addRenderableWidget(leftSteveButton);
        this.addRenderableWidget(rightSteveButton);
        this.addRenderableWidget(resetSteveButton);

        this.addRenderableWidget(upTempReadoutButton);
        this.addRenderableWidget(downTempReadoutButton);
        this.addRenderableWidget(leftTempReadoutButton);
        this.addRenderableWidget(rightTempReadoutButton);
        this.addRenderableWidget(resetTempReadoutButton);

        this.addRenderableWidget(iconBobbingButton);
        this.addRenderableWidget(customHotbarButton);
        this.addRenderableWidget(gracePeriodButton);

        if (mc.player == null || mc.player.getPermissionLevel() >= 2)
        {
            this.addRenderableWidget(gracePeriodLengthInput);
        } else
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

        drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.grace_period_length.name"), this.width / 2 - 185,
                gracePeriodLengthInput.y + 6, ConfigScreen.textOptionColor());

        drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.temperature_icon.name"), this.width / 2 + 51,
                this.height / 4 - 2, 16777215);
        drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.temperature_readout.name"), this.width / 2 + 51,
                this.height / 4 - 2 + (int) (ConfigScreen.OPTION_SIZE * 1.5), 16777215);
        drawString(PoseStack, this.font, new TranslatableComponent("cold_sweat.config.offset_shift.name"), this.width / 2 + 51,
                this.height / 4 + 128, 16777215);
    }

    @Override
    public void onClose()
    {
        super.onClose();
        configCache.gracePeriodLength = Integer.parseInt(gracePeriodLengthInput.getValue());
        configCache.gracePeriodEnabled = gracePeriod;
        ConfigScreen.saveConfig(configCache);
    }

    private void changeSelfIndicatorPos(int axis, int amount)
    {
        if (Screen.hasShiftDown()) amount *= 10;
        if (axis == 0)
        {
            ClientSettingsConfig.getInstance().setSteveHeadX(ClientSettingsConfig.getInstance().steveHeadX() + amount);
        } else if (axis == 1)
        {
            ClientSettingsConfig.getInstance().setSteveHeadY(ClientSettingsConfig.getInstance().steveHeadY() + amount);
        }
    }

    private void resetSelfIndicatorPos()
    {
        ClientSettingsConfig.getInstance().setSteveHeadX(0);
        ClientSettingsConfig.getInstance().setSteveHeadY(0);
    }

    private void changeTempReadoutPos(int axis, int amount)
    {
        if (Screen.hasShiftDown()) amount *= 10;
        if (axis == 0)
        {
            ClientSettingsConfig.getInstance().setTempGaugeX(ClientSettingsConfig.getInstance().tempGaugeX() + amount);
        } else if (axis == 1)
        {
            ClientSettingsConfig.getInstance().setTempGaugeY(ClientSettingsConfig.getInstance().tempGaugeY() + amount);
        }
    }

    private void resetTempReadoutPos()
    {
        ClientSettingsConfig.getInstance().setTempGaugeX(0);
        ClientSettingsConfig.getInstance().setTempGaugeY(0);
    }

    private void toggleCustomHotbar()
    {
        this.customHotbar = !this.customHotbar;
        customHotbarButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.custom_hotbar.name").getString() + ": " +
                (this.customHotbar ? ON : OFF)));
        ClientSettingsConfig.getInstance().setCustomHotbar(this.customHotbar);
        RearrangeHotbar.customHotbar = this.customHotbar;
    }

    private void toggleIconBobbing()
    {
        this.iconBobbing = !this.iconBobbing;
        iconBobbingButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.icon_bobbing.name").getString() + ": " +
                (this.iconBobbing ? ON : OFF)));
        ClientSettingsConfig.getInstance().setIconBobbing(this.iconBobbing);
    }

    public void toggleGracePeriod()
    {
        gracePeriod = !gracePeriod;
        gracePeriodButton.setMessage(new TextComponent(new TranslatableComponent("cold_sweat.config.grace_period.name").getString() + ": " +
                (gracePeriod ? ON : OFF)));
    }
}
