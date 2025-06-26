package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainSettingsConfig
{
    private static final CSConfigSpec SPEC;
    private static final CSConfigSpec.Builder BUILDER = new CSConfigSpec.Builder();

    public static final CSConfigSpec.ConfigValue<String> DIFFICULTY;
    public static final CSConfigSpec.ConfigValue<String> VERSION;
    public static final CSConfigSpec.ConfigValue<Boolean> AUTO_UPDATE;

    public static final CSConfigSpec.ConfigValue<Double> MAX_HABITABLE_TEMPERATURE;
    public static final CSConfigSpec.ConfigValue<Double> MIN_HABITABLE_TEMPERATURE;
    public static final CSConfigSpec.ConfigValue<Double> TEMP_RATE_MULTIPLIER;
    public static final CSConfigSpec.ConfigValue<Double> TEMP_DAMAGE;

    public static final CSConfigSpec.ConfigValue<Boolean> NULLIFY_IN_PEACEFUL;

    public static final CSConfigSpec.ConfigValue<Integer> GRACE_PERIOD_LENGTH;
    public static final CSConfigSpec.ConfigValue<Boolean> ENABLE_GRACE_PERIOD;

    public static final CSConfigSpec.ConfigValue<Double> HEATSTROKE_FOG;
    public static final CSConfigSpec.ConfigValue<Double> FREEZING_HEARTS;
    public static final CSConfigSpec.ConfigValue<Double> COLD_KNOCKBACK;
    public static final CSConfigSpec.ConfigValue<Double> COLD_MINING;
    public static final CSConfigSpec.ConfigValue<Double> COLD_MOVEMENT;

    public static final CSConfigSpec.ConfigValue<List<? extends String>> DISABLED_TEMP_MODIFIERS;
    public static final CSConfigSpec.DoubleValue MODIFIER_TICK_RATE;
    public static final CSConfigSpec.DoubleValue DRYOFF_SPEED;

    public static final CSConfigSpec.ConfigValue<Double> ACCLIMATION_SPEED;
    public static final CSConfigSpec.ConfigValue<List<? extends Number>> MIN_ACCLIMATION_RANGE;
    public static final CSConfigSpec.ConfigValue<List<? extends Number>> MAX_ACCLIMATION_RANGE;

    static 
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        BUILDER.push("Builtin");

        DIFFICULTY = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " This setting doesn't do anything. Change the difficulty using the in-game menu",
                         " ⌄ ")
                .define(List.of("Difficulty"), () -> defaultDiff.getId(),
                        obj -> obj instanceof String && ConfigSettings.Difficulty.byId((String) obj) != null,
                        String.class);

        VERSION = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The current version of Cold Sweat. This is used by the auto-updater",
                         " ⌄ ")
                .define(List.of("Version"), () -> ColdSweat.getVersion(), obj -> obj instanceof String, String.class);

        AUTO_UPDATE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allows Cold Sweat's configs to be automatically updated with new additions & formatting changes",
                         " ⌄ ")
                .define("Auto Update", true);

        BUILDER.pop();

        BUILDER.push("Difficulty");

        MIN_HABITABLE_TEMPERATURE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines the minimum habitable temperature",
                         " ⌄ ")
                .defineInRange("Minimum Habitable Temperature", defaultDiff.getOrDefault(ConfigSettings.MIN_TEMP, Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        MAX_HABITABLE_TEMPERATURE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines the maximum habitable temperature",
                         " ⌄ ")
                .defineInRange("Maximum Habitable Temperature", defaultDiff.getOrDefault(ConfigSettings.MAX_TEMP, Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        TEMP_RATE_MULTIPLIER = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Rate at which the player's body temperature changes (default: 1.0 (100%))",
                         " ⌄ ")
                .defineInRange("Rate Multiplier", defaultDiff.getOrDefault(ConfigSettings.TEMP_RATE, 1d), 0d, Double.POSITIVE_INFINITY);

        TEMP_DAMAGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Damage dealt to the player when they are too hot or too cold",
                         " ⌄ ")
                .defineInRange("Temperature Damage", defaultDiff.getOrDefault(ConfigSettings.TEMP_DAMAGE, 2d), 0d, Double.POSITIVE_INFINITY);

        NULLIFY_IN_PEACEFUL = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Nullifies temperature damage and effects when the difficulty is set to peaceful",
                         " ⌄ ")
                .define("Damage Scaling", defaultDiff.getOrDefault(ConfigSettings.USE_PEACEFUL_MODE, true));

        BUILDER.pop();

        /*
         Temperature effects
         */
        BUILDER.push("Temperature Effects");
            BUILDER.push("Hot");

            HEATSTROKE_FOG = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines the distance at which the player's vision is obscured by heatstroke fog",
                         " Set to a value above 64 to disable the effect",
                         " ⌄ ")
                .defineInRange("Heatstroke Fog", defaultDiff.getOrDefault(ConfigSettings.HEATSTROKE_FOG_DISTANCE, 6.0), 0, Double.POSITIVE_INFINITY);

            BUILDER.pop();

            BUILDER.push("Cold");

            FREEZING_HEARTS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Up to a certain portion of the player's hearts will freeze over when they are too cold, preventing regeneration",
                         " Represented as a percentage in decimal form",
                         " ⌄ ")
                .defineInRange("Max Frozen Health Percentage", defaultDiff.getOrDefault(ConfigSettings.HEARTS_FREEZING_PERCENTAGE, 0.5), 0, 1);

            COLD_KNOCKBACK = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The player's attack knockback will be reduced by this amount when they are too cold",
                         " Represented as a percentage in decimal form",
                         " ⌄ ")
                .defineInRange("Freezing Knockback Reduction", defaultDiff.getOrDefault(ConfigSettings.COLD_KNOCKBACK_REDUCTION, 0.5), 0, 1);

            COLD_MOVEMENT = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The player's movement speed will be reduced by this amount when they are too cold",
                         " Represented as a percentage in decimal form",
                         " ⌄ ")
                .defineInRange("Freezing Sluggishness", defaultDiff.getOrDefault(ConfigSettings.COLD_MOVEMENT_SLOWDOWN, 0.5), 0, 1);

            COLD_MINING = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The player's mining speed will be reduced by this amount when they are too cold",
                         " Represented as a percentage in decimal form",
                         " ⌄ ")
                .defineInRange("Freezing Mining Impairment", defaultDiff.getOrDefault(ConfigSettings.COLD_MINING_IMPAIRMENT, 0.5), 0, 1);

            BUILDER.pop();

            BUILDER.push("Acclimation");

            ACCLIMATION_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The speed at which the player acclimates to hot or cold environments",
                         " ⌄ ")
                .defineInRange("Acclimation Speed", defaultDiff.getOrDefault(ConfigSettings.ACCLIMATION_SPEED, 0.0011), 0, Double.POSITIVE_INFINITY);

            MIN_ACCLIMATION_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The offset to the player's minimum habitable temperature when they acclimate to cold (first value) or hot (second value) environments",
                         " ⌄ ")
                .defineList("Min Acclimation Range", List.of(-0.46, 0.23), o -> o instanceof Number);

            MAX_ACCLIMATION_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The offset to the player's maximum habitable temperature when they acclimate to cold (first value) or hot (second value) environments",
                         " ⌄ ")
                .defineList("Max Acclimation Range", List.of(-0.23, 0.46), o -> o instanceof Number);

            BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("Grace Period");

                GRACE_PERIOD_LENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The amount of time (in ticks) after the player spawns during which they are immune to temperature effects",
                         " ⌄ ")
                .defineInRange("Grace Period Length", defaultDiff.getOrDefault(ConfigSettings.GRACE_LENGTH, 6000), 0, Integer.MAX_VALUE);

                ENABLE_GRACE_PERIOD = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Enables the grace period",
                         " ⌄ ")
                .define("Grace Period Enabled", defaultDiff.getOrDefault(ConfigSettings.GRACE_ENABLED, true));

        BUILDER.pop();

        BUILDER.push("Misc");

        DISABLED_TEMP_MODIFIERS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Add TempModifier IDs to this list to disable them",
                         " Allows for more granular control of Cold Sweat's features",
                         " Run \"/temp debug @s <trait>\" to see IDs of all modifiers affecting the player",
                         " See the Cold Sweat documentation for a list of default TempModifiers",
                         " ⌄ ")
                .defineListAllowEmpty("Disabled Temperature Modifiers", List.of(), o -> o instanceof String);

        MODIFIER_TICK_RATE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Changes the update rate for temperature modifiers on entities",
                         " Temperature modifiers control most of Cold Sweat's behavior, so lowering this value will improve performance at the cost of responsiveness",
                         " ⌄ ")
                .defineInRange("Modifier Tick Rate", 1.0, 0.1, 1.0);

        DRYOFF_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " A multiplier to the speed at which wet players dry off",
                         " ⌄ ")
                .defineInRange("Dryoff Speed", 1.0, 0.0, Double.POSITIVE_INFINITY);

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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/main.toml");
    }

    public static void save()
    {   SPEC.save();
    }
}
