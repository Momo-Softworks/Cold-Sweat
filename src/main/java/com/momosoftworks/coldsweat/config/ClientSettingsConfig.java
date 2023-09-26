package com.momosoftworks.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ClientSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue celsius;
    private static final ForgeConfigSpec.IntValue tempOffset;

    private static final ForgeConfigSpec.IntValue bodyIconX;
    private static final ForgeConfigSpec.IntValue bodyIconY;

    private static final ForgeConfigSpec.IntValue bodyReadoutX;
    private static final ForgeConfigSpec.IntValue bodyReadoutY;

    private static final ForgeConfigSpec.IntValue worldGaugeX;
    private static final ForgeConfigSpec.IntValue worldGaugeY;

    private static final ForgeConfigSpec.BooleanValue customHotbarLayout;
    private static final ForgeConfigSpec.BooleanValue iconBobbing;

    private static final ForgeConfigSpec.BooleanValue hearthDebug;

    private static final ForgeConfigSpec.BooleanValue showConfigButton;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> configButtonPos;
    static final ForgeConfigSpec.BooleanValue distortionEffects;


    static 
    {
        showConfigButton = BUILDER
                .comment("Show the config menu button in the Options menu")
                .define("Enable In-Game Config", true);
        configButtonPos = BUILDER
                .comment("The position (offset) of the config button on the screen")
                .defineList("Config Button Position", Arrays.asList(0, 0),
                it -> it instanceof Integer);

        /*
         Temperature display preferences
         */
        BUILDER.push("Temperature display preferences");
        celsius = BUILDER
                .comment("Sets all temperatures to be displayed in Celsius")
                .define("Celsius", false);
        tempOffset = BUILDER
                .comment("(Visually) offsets the temperature for personalization (default: 0, so a Plains biome is 75 °F or 21 °C)")
                .defineInRange("Temperature Offset", 0, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        /*
         Position of the "Steve Head" temperature icon above the hotbar
         */
        BUILDER.push("Position of the 'Steve Head' temperature gauge above the hotbar");
        bodyIconX = BUILDER
                .comment("The x position of the gauge relative to its normal position")
                .defineInRange("X Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        bodyIconY = BUILDER
                .comment("The y position of the gauge relative to its normal position")
                .defineInRange("Y Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();


        BUILDER.push("Position of the temperature number below the icon");
        bodyReadoutX = BUILDER
                .comment("The x position of the temperature gauge relative to default")
                .defineInRange("X Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        bodyReadoutY = BUILDER
                .comment("The y position of the temperature gauge relative to default")
                .defineInRange("Y Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Position of the world temperature gauge beside the hotbar");
        worldGaugeX = BUILDER
                .comment("The x position of the temperature gauge relative to default")
                .defineInRange("X Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        worldGaugeY = BUILDER
                .comment("The y position of the temperature gauge relative to default")
                .defineInRange("Y Offset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("UI Options");
        customHotbarLayout = BUILDER
            .define("Custom hotbar layout", true);
        iconBobbing = BUILDER
            .comment("Controls whether the temperature icon shakes when in critical condition")
            .define("Icon Bobbing", true);
        BUILDER.pop();

        hearthDebug = BUILDER
            .comment("Displays areas that the Hearth affecting when the F3 debug menu is open")
            .define("Hearth Debug", true);

        distortionEffects = BUILDER
                .comment("Enables visual distortion effects when the player is too hot or cold")
                .define("Distortion Effects", true);

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

    public boolean isCelsius() {
        return celsius.get();
    }

    public int getTempOffset() {
        return tempOffset.get();
    }

    public int getBodyIconX() {
        return bodyIconX.get();
    }
    public int getBodyIconY() {
        return bodyIconY.get();
    }

    public int getBodyReadoutX() {
        return bodyReadoutX.get();
    }
    public int getBodyReadoutY() {
        return bodyReadoutY.get();
    }

    public int getWorldGaugeX() {
        return worldGaugeX.get();
    }
    public int getWorldGaugeY() {
        return worldGaugeY.get();
    }

    public boolean customHotbarEnabled() {
        return customHotbarLayout.get();
    }

    public boolean isIconBobbingEnabled() {
        return iconBobbing.get();
    }

    public boolean isHearthDebugEnabled() {
        return hearthDebug.get();
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

    public void setBodyIconX(int pos) {
        bodyIconX.set(pos);
    }
    public void setBodyIconY(int pos) {
        bodyIconY.set(pos);
    }

    public void setBodyReadoutX(int pos) {
        bodyReadoutX.set(pos);
    }
    public void setBodyReadoutY(int pos) {
        bodyReadoutY.set(pos);
    }

    public void setWorldGaugeX(int pos) {
        worldGaugeX.set(pos);
    }
    public void setWorldGaugeY(int pos) {
        worldGaugeY.set(pos);
    }

    public void setCustomHotbar(boolean enabled) {
        customHotbarLayout.set(enabled);
    }

    public void setIconBobbing(boolean enabled) {
        iconBobbing.set(enabled);
    }

    public void setHearthDebug(boolean enabled) {
        hearthDebug.set(enabled);
    }

    public boolean isConfigButtonEnabled()
    {   return showConfigButton.get();
    }
    public List<? extends Integer> getConfigButtonPos()
    {   return configButtonPos.get();
    }
    public void setConfigButtonPos(List<Integer> pos)
    {   configButtonPos.set(pos);
    }

    public boolean areDistortionsEnabled()
    {
        return ClientSettingsConfig.distortionEffects.get();
    }
    public void setDistortionsEnabled(boolean sway)
    {
        ClientSettingsConfig.distortionEffects.set(sway);
    }


    public void save() {
        SPEC.save();
    }
}
