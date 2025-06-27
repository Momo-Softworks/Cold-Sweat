package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.Vec2i;
import com.momosoftworks.coldsweat.config.enums.InsulationVisibility;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientSettingsConfig
{
    private static final CSConfigSpec SPEC;
    private static final CSConfigSpec.Builder BUILDER = new CSConfigSpec.Builder();

    public static final CSConfigSpec.BooleanValue USE_CELSIUS;
    public static final CSConfigSpec.IntValue TEMPERATURE_OFFSET;
    public static final CSConfigSpec.DoubleValue TEMPERATURE_SMOOTHING;

    public static final CSConfigSpec.ConfigValue<List<? extends Integer>> BODY_TEMP_ICON_POS;
    public static final CSConfigSpec.BooleanValue SHOW_BODY_TEMP_ICON;
    public static final CSConfigSpec.BooleanValue MOVE_BODY_TEMP_ICON_ADVANCED;

    public static final CSConfigSpec.ConfigValue<List<? extends Integer>> BODY_TEMP_READOUT_POS;
    public static final CSConfigSpec.BooleanValue SHOW_BODY_TEMP_READOUT;

    public static final CSConfigSpec.ConfigValue<List<? extends Integer>> WORLD_TEMP_GAUGE_POS;
    public static final CSConfigSpec.BooleanValue SHOW_WORLD_TEMP_GAUGE;

    public static final CSConfigSpec.BooleanValue USE_CUSTOM_HOTBAR_LAYOUT;
    public static final CSConfigSpec.BooleanValue ENABLE_ICON_BOBBING;

    public static final CSConfigSpec.BooleanValue SHOW_HEARTH_DEBUG_VISUALS;

    public static final CSConfigSpec.BooleanValue SHOW_CONFIG_BUTTON;
    public static final CSConfigSpec.ConfigValue<List<? extends Integer>> CONFIG_BUTTON_POS;
    public static final CSConfigSpec.BooleanValue SHOW_SCREEN_DISTORTIONS;

    public static final CSConfigSpec.BooleanValue HIGH_CONTRAST_MODE;

    public static final CSConfigSpec.BooleanValue ENABLE_CREATIVE_WARNING;

    public static final CSConfigSpec.ConfigValue<String> INSULATION_VISIBILITY;
    public static final CSConfigSpec.BooleanValue EXPAND_TOOLTIPS;

    public static final CSConfigSpec.IntValue WATER_EFFECT_SETTING;
    public static final CSConfigSpec.ConfigValue<List<? extends Integer>> WATER_DROPLET_SCALE;

    public static final CSConfigSpec.BooleanValue SHOW_FROZEN_HEALTH;
    public static final CSConfigSpec.DoubleValue FREEZING_OVERLAY_OPACITY;
    public static final CSConfigSpec.DoubleValue SHIVER_INTENSITY;
    public static final CSConfigSpec.DoubleValue HEATSTROKE_BORDER_OPACITY;
    public static final CSConfigSpec.DoubleValue HEATSTROKE_BLUR;
    public static final CSConfigSpec.DoubleValue HEATSTROKE_SWAY_AMOUNT;
    public static final CSConfigSpec.DoubleValue HEATSTROKE_SWAY_SPEED;

    public static final CSConfigSpec.BooleanValue ANIMATED_SOUL_SPRING_LAMP;

    public static final CSConfigSpec.ConfigValue<String> WATERSKIN_DRINK_PRIMARY;
    public static final CSConfigSpec.ConfigValue<String> WATERSKIN_DRINK_SECONDARY;

    static 
    {
        BUILDER.push("Preferences");
            USE_CELSIUS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Sets all temperatures to be displayed in Celsius",
                             " ⌄ ")
                    .define("Celsius", false);
            TEMPERATURE_OFFSET = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Visually offsets the world temperature to better match the user's definition of \"hot\" and \"cold\"",
                             " ⌄ ")
                    .defineInRange("Temperature Offset", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            TEMPERATURE_SMOOTHING = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The amount of smoothing applied to gauges in the UI",
                             " A value of 1 has no smoothing",
                             " ⌄ ")
                    .defineInRange("Temperature Smoothing", 10, 1.0, Integer.MAX_VALUE);
            WATER_EFFECT_SETTING = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Displays a dripping water effect on-screen and/or with particles when the player is wet",
                             " 0: Off, 1: Particles, 2: On-Screen, 3: Both",
                             " ⌄ ")
                    .defineInRange("Show Water Effect", 3, 0, 3);
            WATER_DROPLET_SCALE = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The scale of the water droplets on-screen, in scaled \"pixels\"",
                             " ⌄ ")
                    .defineList("Water Droplet Scale", List.of(40, 48), it -> it instanceof Integer);
            WATERSKIN_DRINK_PRIMARY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Defines the primary (right-click) action for the waterskin",
                             " Can be one of \"drink\", \"pour\", or \"none\"",
                             " ⌄ ")
                    .define("Waterskin Primary Action", "pour");
            WATERSKIN_DRINK_SECONDARY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Defines the secondary (sneak-right-click) action for the waterskin",
                             " Can be one of \"drink\", \"pour\", or \"none\"",
                             " ⌄ ")
                    .define("Waterskin Secondary Action", "drink");
        BUILDER.pop();

        BUILDER.push("UI Options");
            USE_CUSTOM_HOTBAR_LAYOUT = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Tweaks the layout of the hotbar to make foom for Cold Sweat UI elements",
                             " ⌄ ")
                    .define("Custom hotbar layout", true);
            ENABLE_ICON_BOBBING = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Controls whether UI elements will shake when in critical conditions",
                             " ⌄ ")
                    .define("Icon Bobbing", true);

            BODY_TEMP_ICON_POS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The position of the body temperature icon relative to default",
                             " ⌄ ")
                    .defineList("Body Temperature Icon Offset", List.of(0, 0), it -> it instanceof Integer);
            SHOW_BODY_TEMP_ICON = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables the body temperature icon",
                             " ⌄ ")
                    .define("Show Body Temperature Icon", true);
            MOVE_BODY_TEMP_ICON_ADVANCED = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Moves the body temperature icon to make way for the advanced readout when a thermometer is equipped",
                             " ⌄ ")
                    .define("Move Body Icon For Advanced Info", true);

            BODY_TEMP_READOUT_POS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The position of the body temperature readout relative to default",
                             " ⌄ ")
                    .defineList("Body Temperature Readout Offset", List.of(0, 0), it -> it instanceof Integer);
            SHOW_BODY_TEMP_READOUT = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables the body temperature readout",
                             " ⌄ ")
                    .define("Show Body Temperature Readout", true);

            WORLD_TEMP_GAUGE_POS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The position of the world temperature gauge relative to default",
                             " ⌄ ")
                    .defineList("World Temperature UI Offset", List.of(0, 0), it -> it instanceof Integer);
            SHOW_WORLD_TEMP_GAUGE = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables the world temperature gauge",
                             " ⌄ ")
                    .define("Show World Temperature Gauge", true);

            SHOW_CONFIG_BUTTON = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Show the config menu button in the Options menu",
                             " ⌄ ")
                    .define("Enable In-Game Config", true);
            CONFIG_BUTTON_POS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The position (offset) of the config button on the screen",
                             " ⌄ ")
                    .defineList("Config Button Position", List.of(0, 0),
                                it -> it instanceof Integer);
        BUILDER.pop();

        BUILDER.push("Accessibility");
            SHOW_SCREEN_DISTORTIONS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables visual distortion effects when the player is too hot or cold",
                             " ⌄ ")
                    .define("Distortion Effects", true);
            HIGH_CONTRAST_MODE = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables high contrast mode for UI elements",
                             " ⌄ ")
                    .define("High Contrast", false);
            SHOW_FROZEN_HEALTH = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Shows a frozen texture over the player's health bar when they are freezing",
                             " ⌄ ")
                    .define("Show Frozen Health", true);
            FREEZING_OVERLAY_OPACITY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The opacity of the icy overlay when the player is freezing",
                             " Does not affect Vanilla's powder snow overlay",
                             " ⌄ ")
                    .defineInRange("Freezing Overlay Opacity", 1.0, 0.0, 1.0);
            SHIVER_INTENSITY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The intensity of the shivering effect when the player is too cold",
                             " ⌄ ")
                    .defineInRange("Shiver Intensity", 1.0, 0.0, 2.0);
            HEATSTROKE_BORDER_OPACITY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The opacity of the pulsing red border around the screen when the player is too hot",
                             " ⌄ ")
                    .defineInRange("Heatstroke Border Opacity", 1.0, 0.0, 1.0);
            HEATSTROKE_BLUR = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The amount of blur applied to the screen when the player is too hot",
                             " ⌄ ")
                    .defineInRange("Heatstroke Blur Strength", 1.0, 0.0, 1.0);
            HEATSTROKE_SWAY_AMOUNT = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The amount of camera sway applied when the player is too hot",
                             " ⌄ ")
                    .defineInRange("Heatstroke Sway Amount", 1.0, 0.0, 2.0);
            HEATSTROKE_SWAY_SPEED = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " The speed of the camera sway applied when the player is too hot",
                             " ⌄ ")
                    .defineInRange("Heatstroke Sway Speed", 1.0, 0.0, 2.0);
        BUILDER.pop();

        BUILDER.push("Misc");
            ENABLE_CREATIVE_WARNING = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Warns the player about a bug that clears armor insulation when in creative mode",
                             " ⌄ ")
                    .define("Enable Creative Mode Warning", true);
            SHOW_HEARTH_DEBUG_VISUALS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Displays areas that the Hearth is affecting when the F3 debug menu is open",
                             " ⌄ ")
                    .define("Hearth Debug", true);
            INSULATION_VISIBILITY = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Determines when armor insulation tooltips should show",
                             " One of:",
                             " • \"always\": Always show insulation tooltip",
                             " • \"if_present\": Show insulation tooltip only if the item has insulation (default)",
                             " • \"on_shift\": Show insulation tooltip if shift is held",
                             " • \"shift_and_present\": Show insulation if shift is held AND the armor has any",
                             " • \"never\": Never show insulation tooltips",
                             " ⌄ ")
                    .define("Insulation Tooltip Visibility", "if_present", o -> o instanceof String string && InsulationVisibility.byName(string) != null);
            EXPAND_TOOLTIPS = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Automatically expands all collapsible tooltips",
                             " ⌄ ")
                    .define("Expand Tooltips", false);
            ANIMATED_SOUL_SPRING_LAMP = BUILDER
                    .comment("─────────────────────────────────────────────────────────────────────────",
                             " Enables the animated soul spring lamp model",
                             " Disable this if there are visual errors like missing textures",
                             " ⌄ ")
                    .define("Animated Soul Spring Lamp", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {   Files.createDirectory(csConfigPath);
        }
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "coldsweat/client.toml");
    }

    public static Vec2i getBodyIconPos()
    {   return new Vec2i(BODY_TEMP_ICON_POS.get().get(0), BODY_TEMP_ICON_POS.get().get(1));
    }

    public static Vec2i getBodyReadoutPos()
    {   return new Vec2i(BODY_TEMP_READOUT_POS.get().get(0), BODY_TEMP_READOUT_POS.get().get(1));
    }

    public static Vec2i getWorldGaugePos()
    {   return new Vec2i(WORLD_TEMP_GAUGE_POS.get().get(0), WORLD_TEMP_GAUGE_POS.get().get(1));
    }

    public static Vec2i getConfigButtonPos()
    {   return new Vec2i(CONFIG_BUTTON_POS.get().get(0), CONFIG_BUTTON_POS.get().get(1));
    }

    public static synchronized void writeAndSave()
    {
        USE_CELSIUS.set(ConfigSettings.CELSIUS.get());
        TEMPERATURE_OFFSET.set(ConfigSettings.TEMP_OFFSET.get());
        TEMPERATURE_SMOOTHING.set(ConfigSettings.TEMP_SMOOTHING.get());
        BODY_TEMP_ICON_POS.set(List.of(ConfigSettings.BODY_ICON_POS.get().x(), ConfigSettings.BODY_ICON_POS.get().y()));
        BODY_TEMP_READOUT_POS.set(List.of(ConfigSettings.BODY_READOUT_POS.get().x(), ConfigSettings.BODY_READOUT_POS.get().y()));
        WORLD_TEMP_GAUGE_POS.set(List.of(ConfigSettings.WORLD_GAUGE_POS.get().x(), ConfigSettings.WORLD_GAUGE_POS.get().y()));
        USE_CUSTOM_HOTBAR_LAYOUT.set(ConfigSettings.CUSTOM_HOTBAR_LAYOUT.get());
        ENABLE_ICON_BOBBING.set(ConfigSettings.ICON_BOBBING.get());
        SHOW_HEARTH_DEBUG_VISUALS.set(ConfigSettings.HEARTH_DEBUG.get());
        ENABLE_CREATIVE_WARNING.set(ConfigSettings.SHOW_CREATIVE_WARNING.get());
        SHOW_BODY_TEMP_ICON.set(ConfigSettings.BODY_ICON_ENABLED.get());
        SHOW_BODY_TEMP_READOUT.set(ConfigSettings.BODY_READOUT_ENABLED.get());
        SHOW_WORLD_TEMP_GAUGE.set(ConfigSettings.WORLD_GAUGE_ENABLED.get());
        SHOW_SCREEN_DISTORTIONS.set(ConfigSettings.DISTORTION_EFFECTS.get());
        HIGH_CONTRAST_MODE.set(ConfigSettings.HIGH_CONTRAST.get());
        CONFIG_BUTTON_POS.set(List.of(ConfigSettings.CONFIG_BUTTON_POS.get().x(),
                                        ConfigSettings.CONFIG_BUTTON_POS.get().y()));
        MOVE_BODY_TEMP_ICON_ADVANCED.set(ConfigSettings.MOVE_BODY_ICON_WHEN_ADVANCED.get());
        WATER_EFFECT_SETTING.set(ConfigSettings.WATER_EFFECT_SETTING.get().ordinal());
        save();
    }

    public static synchronized void save()
    {   SPEC.save();
    }
}
