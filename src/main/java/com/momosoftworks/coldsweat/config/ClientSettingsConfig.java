package com.momosoftworks.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClientSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue celsius;
    private static final ForgeConfigSpec.IntValue tempOffset;

    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> bodyIconPos;

    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> bodyReadoutPos;

    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> worldGaugePos;

    private static final ForgeConfigSpec.BooleanValue customHotbarLayout;
    private static final ForgeConfigSpec.BooleanValue iconBobbing;

    private static final ForgeConfigSpec.BooleanValue hearthDebug;

    private static final ForgeConfigSpec.BooleanValue showConfigButton;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> configButtonPos;
    private static final ForgeConfigSpec.BooleanValue distortionEffects;

    private static final ForgeConfigSpec.BooleanValue highContrast;

    private static final ForgeConfigSpec.BooleanValue enableCreativeWarning;


    static 
    {
        /*
         Temperature Display Preferences
         */
        BUILDER.push("Temperature Preferences");
            celsius = BUILDER
                    .comment("Sets all temperatures to be displayed in Celsius")
                    .define("Celsius", false);
            tempOffset = BUILDER
                    .comment("Visually offsets the world temperature to better match the user's definition of \"hot\" and \"cold\"")
                    .defineInRange("Temperature Offset", 0, 0, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("UI Options");
            customHotbarLayout = BUILDER
                .define("Custom hotbar layout", true);
            iconBobbing = BUILDER
                .comment("Controls whether UI elements will shake when in critical conditions")
                .define("Icon Bobbing", true);
            bodyIconPos = BUILDER
                    .comment("The position of the body temperature icon relative to default")
                    .defineList("Body Temperature Icon Offset", List.of(0, 0), it -> it instanceof Integer);
            bodyReadoutPos = BUILDER
                    .comment("The position of the body temperature readout relative to default")
                    .defineList("Body Temperature Readout Offset", List.of(0, 0), it -> it instanceof Integer);
            worldGaugePos = BUILDER
                    .comment("The position of the world temperature gauge relative to default")
                    .defineList("World Temperature UI Offset", List.of(0, 0), it -> it instanceof Integer);
        BUILDER.pop();

        BUILDER.push("Accessibility");
            distortionEffects = BUILDER
                    .comment("Enables visual distortion effects when the player is too hot or cold")
                    .define("Distortion Effects", true);
            highContrast = BUILDER
                    .comment("Enables high contrast mode for UI elements")
                    .define("High Contrast", false);
        BUILDER.pop();

        BUILDER.push("Misc");
            showConfigButton = BUILDER
                    .comment("Show the config menu button in the Options menu")
                    .define("Enable In-Game Config", true);
            configButtonPos = BUILDER
                    .comment("The position (offset) of the config button on the screen")
                    .defineList("Config Button Position", List.of(0, 0),
                                it -> it instanceof Integer);
            enableCreativeWarning = BUILDER
                    .comment("Warns the player about a bug that clears armor insulation when in creative mode")
                    .define("Enable Creative Mode Warning", true);
            hearthDebug = BUILDER
                    .comment("Displays areas that the Hearth is affecting when the F3 debug menu is open")
                    .define("Hearth Debug", true);
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "coldsweat/client.toml");
    }

    public static ClientSettingsConfig getInstance()
    {   return new ClientSettingsConfig();
    }

    /*
     * Non-private values for use elsewhere
     */

    public boolean isHighContrast()
    {   return highContrast.get();
    }
    public void setHighContrast(boolean enabled)
    {   highContrast.set(enabled);
    }

    public boolean isCelsius()
    {   return celsius.get();
    }

    public int getTempOffset()
    {   return tempOffset.get();
    }

    public int getBodyIconX()
    {   return bodyIconPos.get().get(0);
    }
    public int getBodyIconY()
    {   return bodyIconPos.get().get(1);
    }

    public int getBodyReadoutX()
    {   return bodyReadoutPos.get().get(0);
    }
    public int getBodyReadoutY()
    {   return bodyReadoutPos.get().get(1);
    }

    public int getWorldGaugeX()
    {   return worldGaugePos.get().get(0);
    }
    public int getWorldGaugeY()
    {   return worldGaugePos.get().get(1);
    }

    public boolean customHotbarEnabled()
    {   return customHotbarLayout.get();
    }

    public boolean isIconBobbingEnabled()
    {   return iconBobbing.get();
    }

    public boolean isHearthDebugEnabled()
    {   return hearthDebug.get();
    }

    public boolean isCreativeWarningEnabled()
    {   return enableCreativeWarning.get();
    }

    /*
     * Safe set methods for config values
     */

    public void setCelsius(boolean enabled)
    {   celsius.set(enabled);
    }

    public void setTempOffset(int offset)
    {   tempOffset.set(offset);
    }

    public void setBodyIconX(int pos)
    {   bodyIconPos.set(List.of(pos, getBodyIconY()));
    }
    public void setBodyIconY(int pos)
    {   bodyIconPos.set(List.of(getBodyIconX(), pos));
    }

    public void setBodyReadoutX(int pos)
    {   bodyReadoutPos.set(List.of(pos, getBodyReadoutY()));
    }
    public void setBodyReadoutY(int pos)
    {   bodyReadoutPos.set(List.of(getBodyReadoutX(), pos));
    }

    public void setWorldGaugeX(int pos)
    {   worldGaugePos.set(List.of(pos, getWorldGaugeY()));
    }
    public void setWorldGaugeY(int pos)
    {   worldGaugePos.set(List.of(getWorldGaugeX(), pos));
    }

    public void setCustomHotbar(boolean enabled)
    {   customHotbarLayout.set(enabled);
    }

    public void setIconBobbing(boolean enabled)
    {   iconBobbing.set(enabled);
    }

    public void setHearthDebug(boolean enabled)
    {   hearthDebug.set(enabled);
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
    {   return distortionEffects.get();
    }
    public void setDistortionsEnabled(boolean sway)
    {   distortionEffects.set(sway);
    }

    public void setCreativeWarningEnabled(boolean enabled)
    {   enableCreativeWarning.set(enabled);
    }


    public void save()
    {   SPEC.save();
    }
}
