package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final MainSettingsConfig INSTANCE = new MainSettingsConfig();
    private  static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

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
                .defineInRange("Minimum Habitable Temperature", defaultDiff.getOrDefault("min_temp", Temperature.convert(50, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        MAX_HABITABLE_TEMPERATURE = BUILDER
                .comment("Defines the maximum habitable temperature")
                .defineInRange("Maximum Habitable Temperature", defaultDiff.getOrDefault("max_temp", Temperature.convert(100, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        TEMP_RATE_MULTIPLIER = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", defaultDiff.getOrDefault("temp_rate", 1d), 0d, Double.POSITIVE_INFINITY);

        TEMP_DAMAGE = BUILDER
                .comment("Damage dealt to the player when they are too hot or too cold")
                .defineInRange("Temperature Damage", defaultDiff.getOrDefault("temp_damage", 2d), 0d, Double.POSITIVE_INFINITY);

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
                .define("Fire Resistance Immunity", defaultDiff.getOrDefault("fire_resistance_enabled", true));

        ICE_RESISTANCE_BLOCKS_FREEZING = BUILDER
                .comment("Allow ice resistance to block freezing damage")
                .define("Ice Resistance Immunity", defaultDiff.getOrDefault("ice_resistance_enabled", true));

        REQUIRE_THERMOMETER = BUILDER
            .comment("Thermometer item is required to see detailed world temperature")
            .define("Require Thermometer", defaultDiff.getOrDefault("require_thermometer", true));

        BUILDER.pop();


        /*
         Temperature effects
         */
        BUILDER.push("Temperature Effects");
            BUILDER.push("Hot");

            HEATSTROKE_FOG = BUILDER
                .comment("When set to true, the player's view distance will decrease when they are too hot")
                .defineInRange("Heatstroke Fog", defaultDiff.getOrDefault("heatstroke_fog", 6.0), 0, Double.POSITIVE_INFINITY);

            BUILDER.pop();

            BUILDER.push("Cold");

            FREEZING_HEARTS = BUILDER
                .comment("When set to true, this percentage of the player's hearts will freeze over when they are too cold, preventing regeneration",
                         "Represented as a percentage")
                .defineInRange("Freezing Hearts Percentage", defaultDiff.getOrDefault("freezing_hearts", 0.5), 0, 1);

            COLD_KNOCKBACK = BUILDER
                .comment("When set to true, the player's attack knockback will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Knockback Reduction", defaultDiff.getOrDefault("knockback_impairment", 0.5), 0, 1);

            COLD_MOVEMENT = BUILDER
                .comment("When set to true, the player's movement speed will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Movement Slowdown", defaultDiff.getOrDefault("cold_slowness", 0.5), 0, 1);

            COLD_MINING = BUILDER
                .comment("When set to true, the player's mining speed will be reduced by this amount when they are too cold",
                         "Represented as a percentage")
                .defineInRange("Chilled Mining Speed Reduction", defaultDiff.getOrDefault("cold_break_speed", 0.5), 0, 1);

            BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("Grace Period");

                GRACE_PERIOD_LENGTH = BUILDER
                .comment("The number of ticks after the player spawns during which they are immune to temperature effects")
                .defineInRange("Grace Period Length", defaultDiff.getOrDefault("grace_length", 6000), 0, Integer.MAX_VALUE);

                ENABLE_GRACE_PERIOD = BUILDER
                .comment("Enables the grace period")
                .define("Grace Period Enabled", defaultDiff.getOrDefault("grace_enabled", true));

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
