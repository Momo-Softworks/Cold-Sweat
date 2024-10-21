package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final MainSettingsConfig INSTANCE = new MainSettingsConfig();
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Integer> DIFFICULTY;
    public static final ForgeConfigSpec.ConfigValue<String> VERSION;

    public static final ForgeConfigSpec.ConfigValue<Double> MAX_HABITABLE_TEMPERATURE;
    public static final ForgeConfigSpec.ConfigValue<Double> MIN_HABITABLE_TEMPERATURE;
    public static final ForgeConfigSpec.ConfigValue<Double> TEMP_RATE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> TEMP_DAMAGE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FIRE_RESISTANCE_BLOCKS_OVERHEATING;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ICE_RESISTANCE_BLOCKS_FREEZING;

    public static final ForgeConfigSpec.ConfigValue<Boolean> NULLIFY_IN_PEACEFUL;
    public static final ForgeConfigSpec.ConfigValue<Boolean> REQUIRE_THERMOMETER;

    public static final ForgeConfigSpec.ConfigValue<Integer> GRACE_PERIOD_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_GRACE_PERIOD;

    public static final ForgeConfigSpec.ConfigValue<Double> HEATSTROKE_FOG;
    public static final ForgeConfigSpec.ConfigValue<Double> FREEZING_HEARTS;
    public static final ForgeConfigSpec.ConfigValue<Double> COLD_KNOCKBACK;
    public static final ForgeConfigSpec.ConfigValue<Double> COLD_MINING;
    public static final ForgeConfigSpec.ConfigValue<Double> COLD_MOVEMENT;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLED_TEMP_MODIFIERS;

    static 
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        BUILDER.comment("DO NOT EDIT THE SETTINGS IN THIS SECTION")
               .push("Builtin");

        DIFFICULTY = BUILDER
                .defineInRange("Difficulty", defaultDiff.ordinal(), 0, ConfigSettings.Difficulty.values().length - 1);

        VERSION = BUILDER
                .define("Version", "");

        BUILDER.pop();

        /*
         Details about how the player is affected by temperature
         */
        BUILDER.push("Difficulty");

        MIN_HABITABLE_TEMPERATURE = BUILDER
                .comment("Defines the minimum habitable temperature")
                .defineInRange("Minimum Habitable Temperature", defaultDiff.getOrDefault(ConfigSettings.MIN_TEMP, Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        MAX_HABITABLE_TEMPERATURE = BUILDER
                .comment("Defines the maximum habitable temperature")
                .defineInRange("Maximum Habitable Temperature", defaultDiff.getOrDefault(ConfigSettings.MAX_TEMP, Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        TEMP_RATE_MULTIPLIER = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", defaultDiff.getOrDefault(ConfigSettings.TEMP_RATE, 1d), 0d, Double.POSITIVE_INFINITY);

        TEMP_DAMAGE = BUILDER
                .comment("Damage dealt to the player when they are too hot or too cold")
                .defineInRange("Temperature Damage", defaultDiff.getOrDefault(ConfigSettings.TEMP_DAMAGE, 2d), 0d, Double.POSITIVE_INFINITY);

        NULLIFY_IN_PEACEFUL = BUILDER
                .comment("Sets whether damage scales with difficulty")
                .define("Damage Scaling", defaultDiff.getOrDefault("damage_scaling", true));

        BUILDER.pop();


        /*
         Potion effects affecting the player's temperature
         */
        BUILDER.push("Items");

        FIRE_RESISTANCE_BLOCKS_OVERHEATING = BUILDER
                .comment("Allow fire resistance to block overheating damage")
                .define("Fire Resistance Immunity", defaultDiff.getOrDefault(ConfigSettings.FIRE_RESISTANCE_ENABLED, true));

        ICE_RESISTANCE_BLOCKS_FREEZING = BUILDER
                .comment("Allow ice resistance to block freezing damage")
                .define("Ice Resistance Immunity", defaultDiff.getOrDefault(ConfigSettings.ICE_RESISTANCE_ENABLED, true));

        REQUIRE_THERMOMETER = BUILDER
            .comment("Thermometer item is required to see detailed world temperature")
            .define("Require Thermometer", defaultDiff.getOrDefault(ConfigSettings.REQUIRE_THERMOMETER, true));

        BUILDER.pop();


        /*
         Temperature effects
         */
        BUILDER.push("Temperature Effects");
            BUILDER.push("Hot");

            HEATSTROKE_FOG = BUILDER
                .comment("Defines the distance at which the player's vision is obscured by heatstroke fog",
                         "Set to a value above 64 to disable the effect")
                .defineInRange("Heatstroke Fog", defaultDiff.getOrDefault(ConfigSettings.HEATSTROKE_FOG_DISTANCE, 6.0), 0, Double.POSITIVE_INFINITY);

            BUILDER.pop();

            BUILDER.push("Cold");

            FREEZING_HEARTS = BUILDER
                .comment("When set to true, this percentage of the player's hearts will freeze over when they are too cold, preventing regeneration",
                         "Represented as a percentage")
                .defineInRange("Freezing Hearts Percentage", defaultDiff.getOrDefault(ConfigSettings.HEARTS_FREEZING_PERCENTAGE, 0.5), 0, 1);

            COLD_KNOCKBACK = BUILDER
                .comment("When set to true, the player's attack knockback will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Knockback Reduction", defaultDiff.getOrDefault(ConfigSettings.COLD_KNOCKBACK_REDUCTION, 0.5), 0, 1);

            COLD_MOVEMENT = BUILDER
                .comment("When set to true, the player's movement speed will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Movement Slowdown", defaultDiff.getOrDefault(ConfigSettings.COLD_MOVEMENT_SLOWDOWN, 0.5), 0, 1);

            COLD_MINING = BUILDER
                .comment("When set to true, the player's mining speed will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Mining Speed Reduction", defaultDiff.getOrDefault(ConfigSettings.COLD_MINING_IMPAIRMENT, 0.5), 0, 1);

            BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("Grace Period");

                GRACE_PERIOD_LENGTH = BUILDER
                .comment("The amount of time (in ticks) after the player spawns during which they are immune to temperature effects")
                .defineInRange("Grace Period Length", defaultDiff.getOrDefault(ConfigSettings.GRACE_LENGTH, 6000), 0, Integer.MAX_VALUE);

                ENABLE_GRACE_PERIOD = BUILDER
                .comment("Enables the grace period")
                .define("Grace Period Enabled", defaultDiff.getOrDefault(ConfigSettings.GRACE_ENABLED, true));

        BUILDER.pop();

        BUILDER.push("Misc");

        DISABLED_TEMP_MODIFIERS = BUILDER
                .comment("Add TempModifier IDs to this list to disable them",
                         "Allows for more granular control of Cold Sweat's features",
                         " Run \"/temp debug @s <trait>\" to see IDs of all modifiers affecting the player",
                         "",
                         " List of \"Vanilla\" TempModifier IDs:",
                         " cold_sweat:blocks - Disables temperature emitted by blocks",
                         " cold_sweat:biomes - Disables biome temperature (makes all biomes temperate)",
                         " cold_sweat:underground - Disables temperature changes caused by depth / altitude",
                         " cold_sweat:armor - Disables armor insulation",
                         " cold_sweat:mount - Disables insulation from riding an entity",
                         " cold_sweat:waterskin - Disables the waterskin's temperature effects",
                         " cold_sweat:soulspring_lamp - Disables the cooling effect of the soulspring lamp",
                         " cold_sweat:water - Disables the cooling effect of water, and the dripping particles from when the player is wet",
                         " cold_sweat:air_conditioning - Disables the Warmth and Chill effects from the hearth, boiler, and icebox",
                         " cold_sweat:food - Disables temperature effects from eating food",
                         " cold_sweat:freezing - Disables the cooling effect of powder snow",
                         " cold_sweat:on_fire - Disables the heating effect of being on fire",
                         " cold_sweat:soul_sprout - Disables the effects of eating a soul sprout (separate from cold_sweat:food)",
                         " cold_sweat:inventory_items - Disables the temperature effects of items in the player's inventory",
                         " cold_sweat:entities - Disables the temperature emitted from entities",
                         " sereneseasons:season - Disables the temperature effects of seasons from Serene Seasons",
                         " armorunder:lining - Disables the temperature effects of armor linings from Armor Underwear",
                         " weather2:storm - Disables the temperature effects of storms & weather from Weather 2",
                         " curios:curios - Disables the temperature effects of equipped curios")
                .defineListAllowEmpty("Disabled Temperature Modifiers", List.of(), o -> o instanceof String);

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

    public static MainSettingsConfig getInstance()
    {   return INSTANCE;
    }

    /* Getters */

    public String getVersion()
    {   return VERSION.get();
    }
    public int getDifficulty()
    {   return DIFFICULTY.get();
    }

    public boolean isFireResistanceEnabled()
    {   return FIRE_RESISTANCE_BLOCKS_OVERHEATING.get();
    }
    public boolean isIceResistanceEnabled()
    {   return ICE_RESISTANCE_BLOCKS_FREEZING.get();
    }

    public boolean thermometerRequired()
    {   return REQUIRE_THERMOMETER.get();
    }

    public boolean nullifyInPeaceful()
    {   return NULLIFY_IN_PEACEFUL.get();
    }

    public double getTempDamage()
    {   return TEMP_DAMAGE.get();
    }

    public double getMinTempHabitable()
    {   return MIN_HABITABLE_TEMPERATURE.get();
    }
    public double getMaxTempHabitable()
    {   return MAX_HABITABLE_TEMPERATURE.get();
    }

    public double getRateMultiplier()
    {   return TEMP_RATE_MULTIPLIER.get();
    }

    public int getGracePeriodLength()
    {   return GRACE_PERIOD_LENGTH.get();
    }

    public boolean isGracePeriodEnabled()
    {   return ENABLE_GRACE_PERIOD.get();
    }

    public double getHeatstrokeFogDistance()
    {   return HEATSTROKE_FOG.get();
    }

    public double getHeartsFreezingPercentage()
    {   return FREEZING_HEARTS.get();
    }
    public double getColdKnockbackReduction()
    {   return COLD_KNOCKBACK.get();
    }
    public double getColdMiningImpairment()
    {   return COLD_MINING.get();
    }
    public double getColdMovementSlowdown()
    {   return COLD_MOVEMENT.get();
    }

    /* Setters */

    public synchronized void setVersion(String version)
    {   synchronized (MainSettingsConfig.VERSION)
        {   MainSettingsConfig.VERSION.set(version);
        }
    }

    public synchronized void setDifficulty(int value)
    {   synchronized (DIFFICULTY)
        {   DIFFICULTY.set(value);
        }
    }

    public synchronized void setMaxHabitable(double temp)
    {   synchronized (MAX_HABITABLE_TEMPERATURE)
        {   MAX_HABITABLE_TEMPERATURE.set(temp);
        }
    }

    public synchronized void setMinHabitable(double temp)
    {   synchronized (MIN_HABITABLE_TEMPERATURE)
        {   MIN_HABITABLE_TEMPERATURE.set(temp);
        }
    }

    public synchronized void setRateMultiplier(double rate)
    {   synchronized (TEMP_RATE_MULTIPLIER)
        {   TEMP_RATE_MULTIPLIER.set(rate);
        }
    }

    public synchronized void setFireResistanceEnabled(boolean isEffective)
    {   synchronized (FIRE_RESISTANCE_BLOCKS_OVERHEATING)
        {   FIRE_RESISTANCE_BLOCKS_OVERHEATING.set(isEffective);
        }
    }

    public synchronized void setIceResistanceEnabled(boolean isEffective)
    {   synchronized (ICE_RESISTANCE_BLOCKS_FREEZING)
        {   ICE_RESISTANCE_BLOCKS_FREEZING.set(isEffective);
        }
    }

    public synchronized void setRequireThermometer(boolean required)
    {   synchronized (REQUIRE_THERMOMETER)
        {   REQUIRE_THERMOMETER.set(required);
        }
    }

    public synchronized void setNullifyInPeaceful(boolean enabled)
    {   synchronized (NULLIFY_IN_PEACEFUL)
        {   NULLIFY_IN_PEACEFUL.set(enabled);
        }
    }

    public synchronized void setTempDamage(double damage)
    {   synchronized (TEMP_DAMAGE)
        {   TEMP_DAMAGE.set(damage);
        }
    }

    public synchronized void setGracePeriodLength(int ticks)
    {   synchronized (GRACE_PERIOD_LENGTH)
        {   GRACE_PERIOD_LENGTH.set(ticks);
        }
    }

    public synchronized void setGracePeriodEnabled(boolean enabled)
    {   synchronized (ENABLE_GRACE_PERIOD)
        {   ENABLE_GRACE_PERIOD.set(enabled);
        }
    }


    public synchronized void setHeatstrokeFogDistance(double distance)
    {   synchronized (HEATSTROKE_FOG)
        {   HEATSTROKE_FOG.set(distance);
        }
    }

    public synchronized void setHeartsFreezingPercentage(double percent)
    {   synchronized (FREEZING_HEARTS)
        {   FREEZING_HEARTS.set(percent);
        }
    }

    public synchronized void setColdKnockbackReduction(double amount)
    {   synchronized (COLD_KNOCKBACK)
        {   COLD_KNOCKBACK.set(amount);
        }
    }

    public synchronized void setColdMiningImpairment(double amount)
    {   synchronized (COLD_MINING)
        {   COLD_MINING.set(amount);
        }
    }

    public synchronized void setColdMovementSlowdown(double amount)
    {   synchronized (COLD_MOVEMENT)
        {   COLD_MOVEMENT.set(amount);
        }
    }

    public void save()
    {   SPEC.save();
    }
}
