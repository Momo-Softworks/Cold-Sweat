package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.momostudios.coldsweat.ColdSweat;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;

public class ColdSweatConfig
{
    private static final int DEFAULT_HUD_X = 2;
    private static final int DEFAULT_HUD_Y = 2;

    private static final ColdSweatConfig INSTANCE;
    private static final ForgeConfigSpec SPEC;
    private static final Path CONFIG_PATH = Paths.get("config/cold-sweat_main.toml");

    static
    {
        Pair<ColdSweatConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ColdSweatConfig::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .sync()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();
        config.load();
        config.save();
        SPEC.setConfig(config);
    }

    private final ForgeConfigSpec.BooleanValue celsius;
    private final ForgeConfigSpec.IntValue tempOffset;

    private final ForgeConfigSpec.DoubleValue maxHabitable;
    private final ForgeConfigSpec.DoubleValue minHabitable;
    private final ForgeConfigSpec.DoubleValue rateMultiplier;

    private final ForgeConfigSpec.BooleanValue fireResistanceEffect;
    private final ForgeConfigSpec.BooleanValue iceResistanceEffect;

    private final ForgeConfigSpec.IntValue steveHeadX;
    private final ForgeConfigSpec.IntValue steveHeadY;

    private final ForgeConfigSpec.IntValue tempGaugeX;
    private final ForgeConfigSpec.IntValue tempGaugeY;

    private final ForgeConfigSpec.BooleanValue customHotbarLayout;
    private final ForgeConfigSpec.BooleanValue iconBobbing;

    public final ForgeConfigSpec.BooleanValue animalsTemperature;
    public final ForgeConfigSpec.BooleanValue damageScaling;
    public final ForgeConfigSpec.BooleanValue requireThermometer;

    private ColdSweatConfig(ForgeConfigSpec.Builder configSpecBuilder)
    {
        /*
          Temperature display preferences
         */
        configSpecBuilder.push("Temperature display preferences");
        celsius = configSpecBuilder
                .comment("Sets all temperatures to be displayed in Celsius")
                .define("Celsius", false);

        tempOffset = configSpecBuilder
                .comment("(Visually) offsets the temperature for personalization (default: 0, so a Plains biome is 75 \u00b0F or 21 \u00b0C)")
                .defineInRange("Temperature Offset", 0, 0, Integer.MAX_VALUE);
        configSpecBuilder.pop();

        /*
          Potion effects affecting the player's temperature
         */
        configSpecBuilder.push("Item settings");
        fireResistanceEffect = configSpecBuilder
                .comment("Fire Resistance blocks all hot temperatures")
                .define("Fire Resistance Immunity", true);
        iceResistanceEffect = configSpecBuilder
                .comment("Ice Resistance blocks all cold temperatures")
                .define("Ice Resistance Immunity", true);
        requireThermometer = configSpecBuilder
            .comment("Thermometer item is required to see ambient temperature")
            .define("Require Thermometer", true);
        configSpecBuilder.pop();

        /*
          Position of the "Steve Head" temperature gauge above the hotbar
         */
        configSpecBuilder.push("Position of the 'Steve Head' temperature gauge above the hotbar");
        steveHeadX = configSpecBuilder
                .comment("The x position of the gauge relative to its normal position")
                .defineInRange("Steve Head X Offset", 0, 0, Integer.MAX_VALUE);
        steveHeadY = configSpecBuilder
                .comment("The y position of the gauge relative to its normal position")
                .defineInRange("Steve Head Y Offset", 0, 0, Integer.MAX_VALUE);
        configSpecBuilder.pop();


        configSpecBuilder.push("Position of the actual number temperature gauge above the hotbar");
        tempGaugeX = configSpecBuilder
                .comment("The x position of the temperature gauge relative to default")
                .defineInRange("Temp Gauge X Offset", 0, 0, Integer.MAX_VALUE);
        tempGaugeY = configSpecBuilder
                .comment("The y position of the temperature gauge relative to default")
                .defineInRange("Temp Gauge Y Offset", 0, 0, Integer.MAX_VALUE);
        configSpecBuilder.pop();

        configSpecBuilder.push("UI Options");
        customHotbarLayout = configSpecBuilder
            .define("Custom hotbar layout", true);
        iconBobbing = configSpecBuilder
            .comment("Controls whether the temperature icon shakes when in critical condition")
            .define("Icon Bobbing", true);
        configSpecBuilder.pop();

        /*
          Misc. things that are affected by temperature
         */
        configSpecBuilder.push("Misc things that are affected by temperature");
        animalsTemperature = configSpecBuilder
                .comment("Sets whether animals are affected by temperature")
                .define("Animals Have Temperature", true);
        damageScaling = configSpecBuilder
            .comment("Sets whether damage scales with difficulty")
            .define("Damage Scaling", true);
        configSpecBuilder.pop();

        /*
          Details about how the player is affected by temperature
         */
        configSpecBuilder.push("Details about how the player is affected by temperature");
        minHabitable = configSpecBuilder
                .comment("Minimum habitable temperature (default: 0.25, on a scale of 0 - 2)")
                .defineInRange("Minimum Habitable Temperature", 0.25, 0, Double.POSITIVE_INFINITY);
        maxHabitable = configSpecBuilder
                .comment("Maximum habitable temperature (default: 1.75, on a scale of 0 - 2)")
                .defineInRange("Maximum Habitable Temperature", 1.75, 0, Double.POSITIVE_INFINITY);
        rateMultiplier = configSpecBuilder
                .comment("Rate at which the player's body temperature changes (default: 1.0 (100%))")
                .defineInRange("Rate Multiplier", 1.0, 0, Double.POSITIVE_INFINITY);
        configSpecBuilder.pop();
    }

    public static ColdSweatConfig getInstance()
    {
        return INSTANCE;
    }


    /*
     * Non-private values for use elsewhere
     */
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

    public boolean animalsTemperature() {
        return animalsTemperature.get();
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

    public void setAnimalsTemperature(boolean areAffected) {
        animalsTemperature.set(areAffected);
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
