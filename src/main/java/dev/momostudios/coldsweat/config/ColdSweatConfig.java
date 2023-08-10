package dev.momostudios.coldsweat.config;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ColdSweatConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final ColdSweatConfig INSTANCE = new ColdSweatConfig();
    public  static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<Integer> difficulty;

    public static final ForgeConfigSpec.ConfigValue<Double> maxHabitable;
    public static final ForgeConfigSpec.ConfigValue<Double> minHabitable;
    public static final ForgeConfigSpec.ConfigValue<Double> rateMultiplier;

    public static final ForgeConfigSpec.ConfigValue<Boolean> fireResistanceEffect;
    public static final ForgeConfigSpec.ConfigValue<Boolean> iceResistanceEffect;

    public static final ForgeConfigSpec.ConfigValue<Boolean> damageScaling;
    public static final ForgeConfigSpec.ConfigValue<Boolean> requireThermometer;
    public static final ForgeConfigSpec.ConfigValue<Boolean> checkSleep;

    public static final ForgeConfigSpec.ConfigValue<Integer> gracePeriodLength;
    public static final ForgeConfigSpec.ConfigValue<Boolean> gracePeriodEnabled;

    public static final ForgeConfigSpec.ConfigValue<Double> hearthEffect;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> hearthSpreadWhitelist;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> hearthSpreadBlacklist;

    public static final ForgeConfigSpec.ConfigValue<Boolean> coldSoulFire;

    public static final ForgeConfigSpec.ConfigValue<Boolean> heatstrokeFog;
    public static final ForgeConfigSpec.ConfigValue<Boolean> freezingHearts;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldKnockback;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldMining;
    public static final ForgeConfigSpec.ConfigValue<Boolean> coldMovement;

    static 
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        /*
         Difficulty
         */
        difficulty = BUILDER
                .comment("Overrides all other config options for easy difficulty management",
                        "This value is changed by the in-game config. It does nothing otherwise.")
                .defineInRange("Difficulty", defaultDiff.ordinal(), 0, ConfigSettings.Difficulty.values().length - 1);

        /*
         Potion effects affecting the player's temperature
         */
        BUILDER.push("Item settings");
        fireResistanceEffect = BUILDER
                .comment("Fire Resistance blocks all hot temperatures")
                .define("Fire Resistance Immunity", defaultDiff.getOrDefault("fire_resistance_enabled", true));
        iceResistanceEffect = BUILDER
                .comment("Ice Resistance blocks all cold temperatures")
                .define("Ice Resistance Immunity", defaultDiff.getOrDefault("ice_resistance_enabled", true));
        requireThermometer = BUILDER
            .comment("Thermometer item is required to see world temperature")
            .define("Require Thermometer", defaultDiff.getOrDefault("require_thermometer", true));
        BUILDER.pop();

        /*
         Misc. things that are affected by temperature
         */
        BUILDER.push("Misc temperature-related things");
        damageScaling = BUILDER
            .comment("Sets whether damage scales with difficulty")
            .define("Damage Scaling", defaultDiff.getOrDefault("damage_scaling", true));
        checkSleep = BUILDER
            .comment("When set to true, players cannot sleep if they are cold or hot enough to die")
            .define("Prevent Sleep When in Danger", defaultDiff.getOrDefault("check_sleep_conditions", true));
        BUILDER.pop();

        /*
         Details about how the player is affected by temperature
         */
        BUILDER.push("Details about how the player is affected by temperature");
        minHabitable = BUILDER
                .comment("Defines the minimum habitable temperature")
                .defineInRange("Minimum Habitable Temperature", defaultDiff.getOrDefault("min_temp", CSMath.convertTemp(50, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        maxHabitable = BUILDER
                .comment("Defines the maximum habitable temperature")
                .defineInRange("Maximum Habitable Temperature", defaultDiff.getOrDefault("max_temp", CSMath.convertTemp(90, Temperature.Units.F, Temperature.Units.MC, true)),
                               Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        rateMultiplier = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", defaultDiff.<Double>getOrDefault("temp_rate", 1d), 0d, Double.POSITIVE_INFINITY);
        BUILDER.pop();


        BUILDER.push("Temperature Effects");
            BUILDER.push("Hot");
            heatstrokeFog = BUILDER
                .comment("When set to true, the player's view distance will decrease when they are too hot")
                .define("Heatstroke Fog", defaultDiff.getOrDefault("heatstroke_fog", true));
            BUILDER.pop();

            BUILDER.push("Cold");
            freezingHearts = BUILDER
                .comment("When set to true, some of the player's hearts will freeze when they are too cold, preventing regeneration")
                .define("Freezing Hearts", defaultDiff.getOrDefault("freezing_hearts", true));
            coldKnockback = BUILDER
                .comment("When set to true, the player's attack knockback will be reduced when they are too cold")
                .define("Cold Knockback Reduction", defaultDiff.getOrDefault("knockback_impairment", true));
            coldMovement = BUILDER
                .comment("When set to true, the player's movement speed will be reduced when they are too cold")
                .define("Cold Slowness", defaultDiff.getOrDefault("cold_slowness", true));
            coldMining = BUILDER
                .comment("When set to true, the player's mining speed will be reduced when they are too cold")
                .define("Cold Mining Fatigue", defaultDiff.getOrDefault("cold_break_speed", true));
            BUILDER.pop();
        BUILDER.pop();


        BUILDER.push("Grace Period Details");
                gracePeriodLength = BUILDER
                .comment("Grace period length in ticks (default: 6000)")
                .defineInRange("Grace Period Length", defaultDiff.getOrDefault("grace_length", 6000), 0, Integer.MAX_VALUE);
                gracePeriodEnabled = BUILDER
                .comment("Enables the grace period (default: true)")
                .define("Grace Period Enabled", defaultDiff.getOrDefault("grace_enabled", true));
        BUILDER.pop();

        BUILDER.push("Hearth");
            hearthEffect = BUILDER
                    .comment("How strong the hearth is (default: 0.5)")
                    .defineInRange("Hearth Strength", defaultDiff.getOrDefault("hearth_strength", 0.5), 0, 1.0);
            hearthSpreadWhitelist = BUILDER
                    .comment("List of blocks that the hearth can spread through",
                             "Use this list if the hearth isn't spreading through particular blocks that it should")
                    .defineList("Hearth Spread Whitelist", Arrays.asList(
                            "minecraft:iron_bars",
                            "#minecraft:leaves"
                    ), o -> o instanceof String);
            hearthSpreadBlacklist = BUILDER
                    .comment("List of blocks that the hearth cannot spread through",
                             "Use this list if the hearth is spreading through particular blocks that it shouldn't")
                    .defineList("Hearth Spread Blacklist", Arrays.asList(
                    ), o -> o instanceof String);
        BUILDER.pop();

        BUILDER.push("Cold Soul Fire");
        coldSoulFire = BUILDER
                .comment("Converts damage dealt by Soul Fire to cold damage (default: true)",
                         "Does not affect the block's temperature")
                .define("Cold Soul Fire", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception e)
        {
            // Do nothing
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/main.toml");
    }

    public static ColdSweatConfig getInstance()
    {
        return INSTANCE;
    }

    /*
     * Non-private values for use elsewhere
     */

    public int   getDifficulty() {
        return difficulty.get();
    }

    public boolean isFireResistanceEnabled() {
        return fireResistanceEffect.get();
    }

    public boolean isIceResistanceEnabled() {
        return iceResistanceEffect.get();
    }

    public boolean thermometerRequired() {
        return requireThermometer.get();
    }

    public boolean doDamageScaling() {
        return damageScaling.get();
    }

    public double getMinTempHabitable() {
        return minHabitable.get();
    }

    public double getMaxTempHabitable() {
        return maxHabitable.get();
    }

    public double getRateMultiplier() {
        return rateMultiplier.get();
    }

    public int getGracePeriodLength()
    {
        return gracePeriodLength.get();
    }

    public boolean isGracePeriodEnabled()
    {
        return gracePeriodEnabled.get();
    }

    public boolean isSoulFireCold()
    {
        return coldSoulFire.get();
    }

    public double getHearthEffect()
    {
        return hearthEffect.get();
    }
    public List<String> getHearthSpreadWhitelist()
    {   return (List<String>) hearthSpreadWhitelist.get();
    }
    public List<String> getHearthSpreadBlacklist()
    {   return (List<String>) hearthSpreadBlacklist.get();
    }

    public boolean isSleepChecked()
    {
        return checkSleep.get();
    }

    public boolean heatstrokeFog()
    {
        return heatstrokeFog.get();
    }

    public boolean freezingHearts()
    {
        return freezingHearts.get();
    }

    public boolean coldKnockback()
    {
        return coldKnockback.get();
    }

    public boolean coldMining()
    {
        return coldMining.get();
    }

    public boolean coldMovement()
    {
        return coldMovement.get();
    }

    /*
     * Safe set methods for config values
     */
    public void setDifficulty(int value)
    {   synchronized (difficulty)
        {   difficulty.set(value);
        }
    }

    public void setMaxHabitable(double temp)
    {   synchronized (maxHabitable)
        {   maxHabitable.set(temp);
        }
    }

    public void setMinHabitable(double temp)
    {   synchronized (minHabitable)
        {   minHabitable.set(temp);
        }
    }

    public void setRateMultiplier(double rate)
    {   synchronized (rateMultiplier)
        {   rateMultiplier.set(rate);
        }
    }

    public void setFireResistanceEnabled(boolean isEffective)
    {   synchronized (fireResistanceEffect)
        {   fireResistanceEffect.set(isEffective);
        }
    }

    public void setIceResistanceEnabled(boolean isEffective)
    {   synchronized (iceResistanceEffect)
        {   iceResistanceEffect.set(isEffective);
        }
    }

    public void setRequireThermometer(boolean required)
    {   synchronized (requireThermometer)
        {   requireThermometer.set(required);
        }
    }

    public void setDamageScaling(boolean enabled)
    {   synchronized (damageScaling)
        {   damageScaling.set(enabled);
        }
    }

    public void setGracePeriodLength(int ticks)
    {   synchronized (gracePeriodLength)
        {   gracePeriodLength.set(ticks);
        }
    }

    public void setGracePeriodEnabled(boolean enabled)
    {   synchronized (gracePeriodEnabled)
        {   gracePeriodEnabled.set(enabled);
        }
    }

    public void setHeatstrokeFog(boolean fog)
    {   synchronized (heatstrokeFog)
        {   heatstrokeFog.set(fog);
        }
    }

    public void setFreezingHearts(boolean hearts)
    {   synchronized (freezingHearts)
        {   freezingHearts.set(hearts);
        }
    }

    public void setColdKnockback(boolean knockback)
    {   synchronized (coldKnockback)
        {   coldKnockback.set(knockback);
        }
    }

    public void setColdMining(boolean mining)
    {   synchronized (coldMining)
        {   coldMining.set(mining);
        }
    }

    public void setColdMovement(boolean movement)
    {   synchronized (coldMovement)
        {   coldMovement.set(movement);
        }
    }

    public void setHearthSpreadWhitelist(List<ResourceLocation> whitelist)
    {   synchronized (hearthSpreadWhitelist)
        {   hearthSpreadWhitelist.set(whitelist.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        }
    }
    public void setHearthSpreadBlacklist(List<ResourceLocation> blacklist)
    {   synchronized (hearthSpreadBlacklist)
        {   hearthSpreadBlacklist.set(blacklist.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        }
    }

    public void save()
    {   SPEC.save();
    }
}
