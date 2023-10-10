package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.List;

public class ClientSettingsConfig
{
    public static Configuration CONFIG;

    public static boolean celsius;
    public static int tempOffset;

    public static String bodyIconPos;

    public static String bodyReadoutPos;

    public static String worldGaugePos;

    public static boolean customHotbarLayout;
    public static boolean iconBobbing;

    public static boolean hearthDebug;

    public static boolean showConfigButton;
    public static String configButtonPos;
    public static boolean distortionEffects;

    public static void loadConfig()
    {
        /*
          Config Button
         */
        showConfigButton = CONFIG.getBoolean("Enable In-Game Config",
                "gui",
                true,
                "Show the config menu button in the Options menu");
        configButtonPos = CONFIG.getString("Config Button Position",
                "gui",
                "[0, 0]",
                "The position (offset) of the config button on the screen");

        /*
         Temperature Display Preferences
         */
        celsius = CONFIG.getBoolean("Celsius",
                "preferences",
                false,
                "Sets all temperatures to be displayed in Celsius");
        tempOffset = CONFIG.getInt("Temperature Offset",
                "preferences",
                0,
                0,
                Integer.MAX_VALUE,
                "Visually offsets the world temperature to better match the user's definition of \"hot\" and \"cold\"");

        /*
         Body Temp Position
         */
        bodyIconPos = CONFIG.getString("gui",
                "Body Icon Offset",
                "[0, 0]",
                "The position of the 'Steve Head' body temperature gauge above the hotbar");

        bodyReadoutPos = CONFIG.getString("Body Readout Offset",
                "gui",
                "[0, 0]",
                "The position of the body temperature readout above the hotbar");

        /*
         World Temp Position
         */
        worldGaugePos = CONFIG.getString("World Gauge Offset",
                "gui",
                "[0, 0]",
                "The position of the world temperature gauge beside the hotbar");

        /*
         UI Options
         */
        customHotbarLayout = CONFIG.getBoolean("Custom hotbar layout",
                "gui",
                true,
                "Use a custom hotbar layout to make room for the temperature gauge");
        iconBobbing = CONFIG.getBoolean("Icon Bobbing",
                "gui",
                true,
                "Controls whether UI elements will shake when in critical conditions");

        /*
         Misc
         */
        hearthDebug = CONFIG.getBoolean("Hearth Debug",
                "misc",
                true,
                "Displays areas that the Hearth is affecting when the F3 debug menu is open");
        distortionEffects = CONFIG.getBoolean("Distortion Effects",
                "misc",
                true,
                "Enables visual distortion effects when the player is too hot or cold");

        if (CONFIG.hasChanged())
        {   CONFIG.save();
        }
    }

    public static void init(String configDir)
    {
        if (configDir != null)
        {   File path = new File(configDir + "/" + ColdSweat.MOD_ID + "/client.cfg");
            CONFIG = new Configuration(path);
            loadConfig();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {   if (event.modID.equals(ColdSweat.MOD_ID))
    {   loadConfig();
    }
    }
}
