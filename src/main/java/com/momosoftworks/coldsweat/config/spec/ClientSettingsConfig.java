package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientSettingsConfig
{
    private static final ModConfigSpec SPEC;
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue celsius;
    private static final ModConfigSpec.IntValue tempOffset;
    private static final ModConfigSpec.DoubleValue tempSmoothing;

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> bodyIconPos;
    private static final ModConfigSpec.BooleanValue bodyIconEnabled;
    private static final ModConfigSpec.BooleanValue moveBodyIconWhenAdvanced;

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> bodyReadoutPos;
    private static final ModConfigSpec.BooleanValue bodyReadoutEnabled;

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> worldGaugePos;
    private static final ModConfigSpec.BooleanValue worldGaugeEnabled;

    private static final ModConfigSpec.BooleanValue customHotbarLayout;
    private static final ModConfigSpec.BooleanValue iconBobbing;

    private static final ModConfigSpec.BooleanValue hearthDebug;

    private static final ModConfigSpec.BooleanValue showConfigButton;
    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> configButtonPos;
    private static final ModConfigSpec.BooleanValue distortionEffects;

    private static final ModConfigSpec.BooleanValue highContrast;

    private static final ModConfigSpec.BooleanValue enableCreativeWarning;

    private static final ModConfigSpec.BooleanValue hideTooltips;


    static 
    {
        /*
         Temperature Display Preferences
         */
        BUILDER.push("Visual Preferences");
            celsius = BUILDER
                    .comment("Sets all temperatures to be displayed in Celsius")
                    .define("Celsius", false);
            tempOffset = BUILDER
                    .comment("Visually offsets the world temperature to better match the user's definition of \"hot\" and \"cold\"")
                    .defineInRange("Temperature Offset", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            tempSmoothing = BUILDER
                    .comment("The amount of smoothing applied to gauges in the UI",
                             "A value of 1 has no smoothing")
                    .defineInRange("Temperature Smoothing", 10, 1.0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("UI Options");
            customHotbarLayout = BUILDER
                    .define("Custom hotbar layout", true);
            iconBobbing = BUILDER
                    .comment("Controls whether UI elements will shake when in critical conditions")
                    .define("Icon Bobbing", true);

            bodyIconPos = BUILDER
                    .comment("The position of the body temperature icon relative to default")
                    .defineList("Body Temperature Icon Offset", List.of(0, 0), it -> it instanceof Integer);
            bodyIconEnabled = BUILDER
                    .comment("Enables the body temperature icon above the hotbar")
                    .define("Show Body Temperature Icon", true);
            moveBodyIconWhenAdvanced = BUILDER
                    .comment("Moves the body temperature icon to make way for the advanced readout when a thermometer is equipped")
                    .define("Move Body Icon For Advanced Info", true);

            bodyReadoutPos = BUILDER
                    .comment("The position of the body temperature readout relative to default")
                    .defineList("Body Temperature Readout Offset", List.of(0, 0), it -> it instanceof Integer);
            bodyReadoutEnabled = BUILDER
                    .comment("Enables the body temperature readout above the hotbar")
                    .define("Show Body Temperature Readout", true);

            worldGaugePos = BUILDER
                    .comment("The position of the world temperature gauge relative to default")
                    .defineList("World Temperature UI Offset", List.of(0, 0), it -> it instanceof Integer);
            worldGaugeEnabled = BUILDER
                    .comment("Enables the world temperature gauge next to the hotbar")
                    .define("Show World Temperature Gauge", true);
        BUILDER.pop();

        BUILDER.push("Accessibility");
            distortionEffects = BUILDER
                    .comment("Enables visual distortion effects when the player is too hot or cold")
                    .define("Distortion Effects", true);
            highContrast = BUILDER
                    .comment("Enables high contrast mode for UI elements")
                    .define("High Contrast", false);
        BUILDER.pop();

        BUILDER.push("Misc");
            showConfigButton = BUILDER
                    .comment("Show the config menu button in the Options menu")
                    .define("Enable In-Game Config", true);
            configButtonPos = BUILDER
                    .comment("The position (offset) of the config button on the screen")
                    .defineList("Config Button Position", List.of(0, 0),
                                it -> it instanceof Integer);
            enableCreativeWarning = BUILDER
                    .comment("Warns the player about a bug that clears armor insulation when in creative mode")
                    .define("Enable Creative Mode Warning", true);
            hearthDebug = BUILDER
                    .comment("Displays areas that the Hearth is affecting when the F3 debug menu is open")
                    .define("Hearth Debug", true);
            hideTooltips = BUILDER
                    .comment("Hides insulation tooltips for items, armor, and curios unless SHIFT is held")
                    .define("Hide Tooltips", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup(ModContainer modContainer)
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {   Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        modContainer.registerConfig(ModConfig.Type.CLIENT, SPEC, "coldsweat/client.toml");
    }

    public static ClientSettingsConfig getInstance()
    {   return new ClientSettingsConfig();
    }

    /*
     * Non-private values for use elsewhere
     */

    public boolean isHighContrast()
    {   return highContrast.get();
    }
    public void setHighContrast(boolean enabled)
    {   highContrast.set(enabled);
    }

    public boolean isCelsius()
    {   return celsius.get();
    }

    public int getTempOffset()
    {   return tempOffset.get();
    }

    public int getBodyIconX()
    {   return bodyIconPos.get().get(0);
    }
    public int getBodyIconY()
    {   return bodyIconPos.get().get(1);
    }

    public int getBodyReadoutX()
    {   return bodyReadoutPos.get().get(0);
    }
    public int getBodyReadoutY()
    {   return bodyReadoutPos.get().get(1);
    }

    public int getWorldGaugeX()
    {   return worldGaugePos.get().get(0);
    }
    public int getWorldGaugeY()
    {   return worldGaugePos.get().get(1);
    }

    public double getTempSmoothing()
    {   return tempSmoothing.get();
    }

    public boolean isCustomHotbarLayout()
    {   return customHotbarLayout.get();
    }

    public boolean isIconBobbing()
    {   return iconBobbing.get();
    }

    public boolean isHearthDebug()
    {   return hearthDebug.get();
    }

    public boolean showCreativeWarning()
    {   return enableCreativeWarning.get();
    }

    public boolean isBodyIconEnabled()
    {   return bodyIconEnabled.get();
    }
    public boolean isBodyReadoutEnabled()
    {   return bodyReadoutEnabled.get();
    }
    public boolean isWorldGaugeEnabled()
    {   return worldGaugeEnabled.get();
    }

    public boolean moveBodyIconWhenAdvanced()
    {   return moveBodyIconWhenAdvanced.get();
    }

    public boolean hideTooltips()
    {   return hideTooltips.get();
    }

    /*
     * Safe set methods for config values
     */

    public void setCelsius(boolean enabled)
    {   celsius.set(enabled);
    }

    public void setTempOffset(int offset)
    {   tempOffset.set(offset);
    }

    public void setBodyIconX(int pos)
    {   bodyIconPos.set(List.of(pos, getBodyIconY()));
    }
    public void setBodyIconY(int pos)
    {   bodyIconPos.set(List.of(getBodyIconX(), pos));
    }

    public void setBodyReadoutX(int pos)
    {   bodyReadoutPos.set(List.of(pos, getBodyReadoutY()));
    }
    public void setBodyReadoutY(int pos)
    {   bodyReadoutPos.set(List.of(getBodyReadoutX(), pos));
    }

    public void setWorldGaugeX(int pos)
    {   worldGaugePos.set(List.of(pos, getWorldGaugeY()));
    }
    public void setWorldGaugeY(int pos)
    {   worldGaugePos.set(List.of(getWorldGaugeX(), pos));
    }

    public void setCustomHotbar(boolean enabled)
    {   customHotbarLayout.set(enabled);
    }

    public void setIconBobbing(boolean enabled)
    {   iconBobbing.set(enabled);
    }

    public void setHearthDebug(boolean enabled)
    {   hearthDebug.set(enabled);
    }

    public boolean showConfigButton()
    {   return showConfigButton.get();
    }
    public List<? extends Integer> getConfigButtonPos()
    {   return configButtonPos.get();
    }
    public void setConfigButtonPos(List<Integer> pos)
    {   configButtonPos.set(pos);
    }

    public boolean areDistortionsEnabled()
    {   return distortionEffects.get();
    }
    public void setDistortionsEnabled(boolean enabled)
    {   distortionEffects.set(enabled);
    }

    public void setCreativeWarningEnabled(boolean enabled)
    {   enableCreativeWarning.set(enabled);
    }

    public void setBodyIconEnabled(boolean enabled)
    {   bodyIconEnabled.set(enabled);
    }
    public void setBodyReadoutEnabled(boolean enabled)
    {   bodyReadoutEnabled.set(enabled);
    }
    public void setWorldGaugeEnabled(boolean enabled)
    {   worldGaugeEnabled.set(enabled);
    }

    public void setTempSmoothing(double smoothing)
    {   tempSmoothing.set(smoothing);
    }

    public void setHideTooltips(boolean hide)
    {   hideTooltips.set(hide);
    }

    public synchronized void writeAndSave()
    {   this.setCelsius(ConfigSettings.CELSIUS.get());
        this.setTempOffset(ConfigSettings.TEMP_OFFSET.get());
        this.setTempSmoothing(ConfigSettings.TEMP_SMOOTHING.get());
        this.setBodyIconX(ConfigSettings.BODY_ICON_POS.get().x());
        this.setBodyIconY(ConfigSettings.BODY_ICON_POS.get().y());
        this.setBodyReadoutX(ConfigSettings.BODY_READOUT_POS.get().x());
        this.setBodyReadoutY(ConfigSettings.BODY_READOUT_POS.get().y());
        this.setWorldGaugeX(ConfigSettings.WORLD_GAUGE_POS.get().x());
        this.setWorldGaugeY(ConfigSettings.WORLD_GAUGE_POS.get().y());
        this.setCustomHotbar(ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get());
        this.setIconBobbing(ConfigSettings.ICON_BOBBING.get());
        this.setHearthDebug(ConfigSettings.HEARTH_DEBUG.get());
        this.setCreativeWarningEnabled(ConfigSettings.SHOW_CREATIVE_WARNING.get());
        this.setBodyIconEnabled(ConfigSettings.BODY_ICON_ENABLED.get());
        this.setBodyReadoutEnabled(ConfigSettings.BODY_READOUT_ENABLED.get());
        this.setWorldGaugeEnabled(ConfigSettings.WORLD_GAUGE_ENABLED.get());
        this.setDistortionsEnabled(ConfigSettings.DISTORTION_EFFECTS.get());
        this.setHighContrast(ConfigSettings.HIGH_CONTRAST.get());
        this.setConfigButtonPos(List.of(ConfigSettings.CONFIG_BUTTON_POS.get().x(),
                                        ConfigSettings.CONFIG_BUTTON_POS.get().y()));
        this.save();
    }

    public synchronized void save()
    {   SPEC.save();
    }
}
