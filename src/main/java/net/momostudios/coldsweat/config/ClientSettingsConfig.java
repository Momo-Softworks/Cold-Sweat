package net.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue celsius;
    private static final ForgeConfigSpec.IntValue tempOffset;

    private static final ForgeConfigSpec.IntValue steveHeadX;
    private static final ForgeConfigSpec.IntValue steveHeadY;

    private static final ForgeConfigSpec.IntValue tempGaugeX;
    private static final ForgeConfigSpec.IntValue tempGaugeY;

    private static final ForgeConfigSpec.BooleanValue customHotbarLayout;
    private static final ForgeConfigSpec.BooleanValue iconBobbing;


    static 
    {
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "coldsweat/client.toml");
    }

    public static ClientSettingsConfig getInstance()
    {
        return new ClientSettingsConfig();
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



    /*
     * Safe set methods for config values
     */

    public void setCelsius(boolean enabled) {
        celsius.set(enabled);
    }

    public void setTempOffset(int offset) {
        tempOffset.set(offset);
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

    public void save() {
        SPEC.save();
    }
}
