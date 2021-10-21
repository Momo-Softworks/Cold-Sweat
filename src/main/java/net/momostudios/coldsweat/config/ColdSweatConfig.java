package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ColdSweatConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue difficulty;

    private static final ForgeConfigSpec.BooleanValue celsius;
    private static final ForgeConfigSpec.IntValue tempOffset;

    private static final ForgeConfigSpec.DoubleValue maxHabitable;
    private static final ForgeConfigSpec.DoubleValue minHabitable;
    private static final ForgeConfigSpec.DoubleValue rateMultiplier;

    private static final ForgeConfigSpec.BooleanValue fireResistanceEffect;
    private static final ForgeConfigSpec.BooleanValue iceResistanceEffect;

    private static final ForgeConfigSpec.IntValue steveHeadX;
    private static final ForgeConfigSpec.IntValue steveHeadY;

    private static final ForgeConfigSpec.IntValue tempGaugeX;
    private static final ForgeConfigSpec.IntValue tempGaugeY;

    private static final ForgeConfigSpec.BooleanValue customHotbarLayout;
    private static final ForgeConfigSpec.BooleanValue iconBobbing;

    private static final ForgeConfigSpec.BooleanValue damageScaling;
    private static final ForgeConfigSpec.BooleanValue requireThermometer;


    static 
    {
        /*
         Difficulty
         */
        difficulty = BUILDER
                .comment("Overrides all other config options for easy difficulty management")
                .defineInRange("Difficulty", 3, 1, 5);

        /*
         Temperature display preferences
         */
        BUILDER.push("Temperature display preferences");
        celsius = BUILDER
                .comment("Sets all temperatures to be displayed in Celsius")
                .define("Celsius", false);
        tempOffset = BUILDER
                .comment("(Visually) offsets the temperature for personalization (default: 0, so a Plains biome is 75 \u00b0F or 21 \u00b0C)")
                .defineInRange("Temperature Offset", 0, 0, Integer.MAX_VALUE);
        BUILDER.pop();

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
        requireThermometer = BUILDER
            .comment("Thermometer item is required to see ambient temperature")
            .define("Require Thermometer", true);
        BUILDER.pop();

        /*
         Position of the "Steve Head" temperature gauge above the hotbar
         */
        BUILDER.push("Position of the 'Steve Head' temperature gauge above the hotbar");
        steveHeadX = BUILDER
                .comment("The x position of the gauge relative to its normal position")
                .defineInRange("Steve Head X Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        steveHeadY = BUILDER
                .comment("The y position of the gauge relative to its normal position")
                .defineInRange("Steve Head Y Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();


        BUILDER.push("Position of the actual number temperature gauge above the hotbar");
        tempGaugeX = BUILDER
                .comment("The x position of the temperature gauge relative to default")
                .defineInRange("Temp Gauge X Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        tempGaugeY = BUILDER
                .comment("The y position of the temperature gauge relative to default")
                .defineInRange("Temp Gauge Y Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("UI Options");
        customHotbarLayout = BUILDER
            .define("Custom hotbar layout", true);
        iconBobbing = BUILDER
            .comment("Controls whether the temperature icon shakes when in critical condition")
            .define("Icon Bobbing", true);
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
                .defineInRange("Minimum Habitable Temperature", 0.25, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        maxHabitable = BUILDER
                .comment("Maximum habitable temperature (default: 1.75, on a scale of 0 - 2)")
                .defineInRange("Maximum Habitable Temperature", 1.75, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        rateMultiplier = BUILDER
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", 1.0, 0, Double.POSITIVE_INFINITY);
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

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/main.toml");
    }

    public static ColdSweatConfig getInstance()
    {
        return new ColdSweatConfig();
    }

    /*
     * Non-private values for use elsewhere
     */
    public int difficulty() {
        return difficulty.get();
    }

    public boolean celsius() {
        return celsius.get();
    }

    public int tempOffset() {
        return tempOffset.get();
    }

    public boolean fireResistanceEffect() {
        return fireResistanceEffect.get();
    }

    public boolean iceResistanceEffect() {
        return iceResistanceEffect.get();
    }

    public boolean requireThermometer() {
        return requireThermometer.get();
    }

    public int steveHeadX() {
        return steveHeadX.get();
    }

    public int steveHeadY() {
        return steveHeadY.get();
    }

    public int tempGaugeX() {
        return tempGaugeX.get();
    }

    public int tempGaugeY() {
        return tempGaugeY.get();
    }

    public boolean customHotbar() {
        return customHotbarLayout.get();
    }

    public boolean iconBobbing() {
        return iconBobbing.get();
    }

    public boolean damageScaling() {
        return damageScaling.get();
    }

    public double minHabitable() {
        return minHabitable.get();
    }

    public double maxHabitable() {
        return maxHabitable.get();
    }

    public double rateMultiplier() {
        return rateMultiplier.get();
    }


    /*
     * Safe set methods for config values
     */
    public void setDifficulty(int value) {
        difficulty.set(value);
    }

    public void setCelsius(boolean enabled) {
        celsius.set(enabled);
    }

    public void setTempOffset(int offset) {
        tempOffset.set(offset);
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

    public void setRequireThermometer(boolean required) {
        requireThermometer.set(required);
    }

    public void setTempGaugeX(int pos) {
        tempGaugeX.set(pos);
    }

    public void setTempGaugeY(int pos) {
        tempGaugeY.set(pos);
    }

    public void setSteveHeadX(int pos) {
        steveHeadX.set(pos);
    }

    public void setSteveHeadY(int pos) {
        steveHeadY.set(pos);
    }

    public void setCustomHotbar(boolean enabled) {
        customHotbarLayout.set(enabled);
    }

    public void setIconBobbing(boolean enabled) {
        iconBobbing.set(enabled);
    }

    public void setDamageScaling(boolean enabled) {
        damageScaling.set(enabled);
    }

    public void save() {
        SPEC.save();
    }
}
