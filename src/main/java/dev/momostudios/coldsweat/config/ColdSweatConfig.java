package dev.momostudios.coldsweat.config;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ColdSweatConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final ColdSweatConfig configReference = new ColdSweatConfig();
    public  static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue difficulty;

    private static final ForgeConfigSpec.DoubleValue maxHabitable;
    private static final ForgeConfigSpec.DoubleValue minHabitable;
    private static final ForgeConfigSpec.DoubleValue rateMultiplier;

    private static final ForgeConfigSpec.BooleanValue fireResistanceEffect;
    private static final ForgeConfigSpec.BooleanValue iceResistanceEffect;

    private static final ForgeConfigSpec.BooleanValue damageScaling;
    private static final ForgeConfigSpec.BooleanValue showWorldTemp;

    private static final ForgeConfigSpec.IntValue gracePeriodLength;
    private static final ForgeConfigSpec.BooleanValue gracePeriodEnabled;

    private static final ForgeConfigSpec.DoubleValue hearthEffect;

    private static final ForgeConfigSpec.BooleanValue showConfigButton;

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> blockEffects;


    static 
    {
        showConfigButton = BUILDER.comment("Show the config menu button in the Options menu").define("ShowConfigButton", true);

        /*
         Difficulty
         */
        difficulty = BUILDER
                .comment("Overrides all other config options for easy difficulty management")
                .defineInRange("Difficulty", 3, 1, 5);

        /*
         Potion effects affecting the player's temperature
         */
        BUILDER.push("Item settings");
        fireResistanceEffect = BUILDER
                .comment("Fire Resistance blocks all hot temperatures")
                .define("Fire Resistance Immunity", true);
        iceResistanceEffect = BUILDER
                .comment("Ice Resistance blocks all cold temperatures")
                .define("Ice Resistance Immunity", true);
        showWorldTemp = BUILDER
            .comment("Thermometer item is required to see world temperature")
            .define("Require Thermometer", true);
        BUILDER.pop();

        /*
         Misc. things that are affected by temperature
         */
        BUILDER.push("Misc things that are affected by temperature");
        damageScaling = BUILDER
            .comment("Sets whether damage scales with difficulty")
            .define("Damage Scaling", true);
        BUILDER.pop();

        /*
         Details about how the player is affected by temperature
         */
        BUILDER.push("Details about how the player is affected by temperature");
        minHabitable = BUILDER
                .comment("Minimum habitable temperature (default: " + CSMath.convertUnits(50, Temperature.Units.F, Temperature.Units.MC, true) + ")")
                .defineInRange("Minimum Habitable Temperature", CSMath.convertUnits(50, Temperature.Units.F, Temperature.Units.MC, true), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        maxHabitable = BUILDER
                .comment("Maximum habitable temperature (default: " + CSMath.convertUnits(100, Temperature.Units.F, Temperature.Units.MC, true) + ")")
                .defineInRange("Maximum Habitable Temperature", CSMath.convertUnits(100, Temperature.Units.F, Temperature.Units.MC, true), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        rateMultiplier = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", 1.0, 0, Double.POSITIVE_INFINITY);
        BUILDER.pop();

        BUILDER.push("Grace Period Details");
                gracePeriodLength = BUILDER
                .comment("Grace period length in ticks (default: 6000)")
                .defineInRange("Grace Period Length", 6000, 0, Integer.MAX_VALUE);
                gracePeriodEnabled = BUILDER
                .comment("Enables the grace period (default: true)")
                .define("Grace Period Enabled", true);
        BUILDER.pop();

        BUILDER.push("Block Effects");
        blockEffects = BUILDER
                .comment("Allows for adding simple BlockEffects without the use of Java mods",
                         "Format (All temperatures are in Minecraft units):",
                         "[\"block ids (separated by \",\")\", <temperature>, <range (max 7)>, <*true/false: weaken over distance>, <*max effect (temperature)>, <*min effect (temperature)>]",
                         "(* = optional) (1 °MC = 42 °F/ 23.33 °C)",
                         "(the effect of the example below (torch) is so low that it does barely anything. It's purely for demonstration)")
                .defineList("BlockEffects", Arrays.asList
                        (
                            Arrays.asList("minecraft:torch,minecraft:wall_torch", 0.01, 4, true, 0.02)
                        ),
                it -> it instanceof List);
        BUILDER.pop();

        BUILDER.push("Hearth Strength");
        hearthEffect = BUILDER
                .comment("How strong the hearth is (default: 0.5)")
                .defineInRange("Hearth Strength", 0.5, 0, 1.0);
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
        return configReference;
    }

    public void writeValues(ConfigCache cache)
    {
        setDifficulty(cache.difficulty);
        setMaxHabitable(cache.maxTemp);
        setMinHabitable(cache.minTemp);
        setRateMultiplier(cache.rate);
        setFireResistanceEnabled(cache.fireRes);
        setIceResistanceEnabled(cache.iceRes);
        setDamageScaling(cache.damageScaling);
        setShowWorldTemp(cache.showWorldTemp);
        setGracePeriodLength(cache.gracePeriodLength);
        setGracePeriodEnabled(cache.gracePeriodEnabled);
        save();
    }

    /*
     * Non-private values for use elsewhere
     */
    public boolean isButtonShowing()
    {
        return showConfigButton.get();
    }

    public int getDifficulty() {
        return difficulty.get();
    }

    public boolean isFireResistanceEnabled() {
        return fireResistanceEffect.get();
    }

    public boolean isIceResistanceEnabled() {
        return iceResistanceEffect.get();
    }

    public boolean isWorldTempShowing() {
        return showWorldTemp.get();
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

    public List<? extends List<Object>> getBlockEffects()
    {
        return blockEffects.get();
    }

    public double getHearthEffect()
    {
        return hearthEffect.get();
    }

    /*
     * Safe set methods for config values
     */
    public void setDifficulty(int value) {
        difficulty.set(value);
    }

    public void setMaxHabitable(double temp) {
        maxHabitable.set(temp);
    }

    public void setMinHabitable(double temp) {
        minHabitable.set(temp);
    }

    public void setRateMultiplier(double rate) {
        rateMultiplier.set(rate);
    }

    public void setFireResistanceEnabled(boolean isEffective) {
        fireResistanceEffect.set(isEffective);
    }

    public void setIceResistanceEnabled(boolean isEffective) {
        iceResistanceEffect.set(isEffective);
    }

    public void setShowWorldTemp(boolean required) {
        showWorldTemp.set(required);
    }

    public void setDamageScaling(boolean enabled) {
        damageScaling.set(enabled);
    }

    public void setGracePeriodLength(int ticks)
    {
        gracePeriodLength.set(ticks);
    }

    public void setGracePeriodEnabled(boolean enabled)
    {
        gracePeriodEnabled.set(enabled);
    }

    public void save() {
        SPEC.save();
    }
}
