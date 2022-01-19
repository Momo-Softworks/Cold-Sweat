package net.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.momostudios.coldsweat.util.MathHelperCS;
import net.momostudios.coldsweat.util.Units;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ColdSweatConfig
{
    private static final ForgeConfigSpec SPEC;
    private static ColdSweatConfig configReference = new ColdSweatConfig();
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue difficulty;

    private static final ForgeConfigSpec.DoubleValue maxHabitable;
    private static final ForgeConfigSpec.DoubleValue minHabitable;
    private static final ForgeConfigSpec.DoubleValue rateMultiplier;

    private static final ForgeConfigSpec.BooleanValue fireResistanceEffect;
    private static final ForgeConfigSpec.BooleanValue iceResistanceEffect;

    private static final ForgeConfigSpec.BooleanValue damageScaling;
    private static final ForgeConfigSpec.BooleanValue showAmbient;

    private static final ForgeConfigSpec.IntValue gracePeriodLength;
    private static final ForgeConfigSpec.BooleanValue gracePeriodEnabled;

    private static final ForgeConfigSpec.BooleanValue showConfigButton;


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
        showAmbient = BUILDER
            .comment("Thermometer item is required to see ambient temperature")
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
                .comment("Minimum habitable temperature (default: 0.25, on a scale of 0 - 2)")
                .defineInRange("Minimum Habitable Temperature", MathHelperCS.convertUnits(50, Units.F, Units.MC, true), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        maxHabitable = BUILDER
                .comment("Maximum habitable temperature (default: 1.75, on a scale of 0 - 2)")
                .defineInRange("Maximum Habitable Temperature", MathHelperCS.convertUnits(100, Units.F, Units.MC, true), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
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
        setFireResistanceEffect(cache.fireRes);
        setIceResistanceEffect(cache.iceRes);
        setDamageScaling(cache.damageScaling);
        setShowAmbient(cache.showAmbient);
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

    public boolean showAmbientGauge() {
        return showAmbient.get();
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

    public void setFireResistanceEffect(boolean isEffective) {
        fireResistanceEffect.set(isEffective);
    }

    public void setIceResistanceEffect(boolean isEffective) {
        iceResistanceEffect.set(isEffective);
    }

    public void setShowAmbient(boolean required) {
        showAmbient.set(required);
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
