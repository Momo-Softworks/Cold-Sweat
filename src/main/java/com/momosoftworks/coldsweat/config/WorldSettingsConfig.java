package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.List;

public class WorldSettingsConfig
{
    public static Configuration CONFIG;
    
    public static String biomeOffsets;
    public static String biomeTemps;
    public static String dimensionOffsets;
    public static String dimensionTemps;

    public static double caveInsulation;

    public static String blockTemps;
    public static int blockRange;

    public static String summerTemps;
    public static String autumnTemps;
    public static String winterTemps;
    public static String springTemps;

    public static void loadConfig()
    {
        /*
         Dimensions
         */
        CONFIG.addCustomCategoryComment("dimensions", "Format: [\"dimension_1\", temperature1], [\"dimension_2\", temperature2], [etc...], [etc...]"
        + "\nCommon dimension IDs: 0 (overworld), -1 (nether), 1 (end)");
            dimensionOffsets = CONFIG.getString("Dimension Temperature Offsets",
                                                "dimensions",
                                                "",
                                                "Applies an offset to the world's temperature across an entire dimension");
            dimensionTemps = CONFIG.getString("Dimension Temperatures",
                                              "dimensions",
                                              "[-1, 0.7], [1, -0.1]",
                                              "Overrides all existing dimension/biome temperatures/offsets");

        /*
         Biomes
         */
        CONFIG.addCustomCategoryComment("biomes", "Format: [\"biome_1\", tempLow, tempHigh, *units], [\"biome_2\", tempLow, tempHigh, *units], [etc...], [etc...]"
        + "\ntemp-low: The temperature of the biome at midnight"
        + "\ntemp-high: The temperature of the biome at noon"
        + "\nunits: Optional. The units of the given temperature(s) (\"C\", \"F\", or \"MC\". Defaults to MC units)");
            biomeOffsets = CONFIG.getString("Biome Temperature Offsets",
                                            "biomes",
                                            "",
                                            "Applies an offset to the temperature of a biome");
            biomeTemps = CONFIG.getString("Biome Temperatures",
                                          "biomes",
                                          "",
                                          "Defines the temperature of a biome, overriding existing biome temperatures & offsets");

        /*
         Block Temperatures
         */
        CONFIG.addCustomCategoryComment("blocks", "Format: [\"block-ids\", <temperature>, <range (max 7)>, <*true/false: falloff>, <*max effect>, <*meta_values>], [etc...], [etc...]"
        + "\n(* = optional) (1 °MC = 42 °F/ 23.33 °C)"
        + "\nArguments:"
        + "\nblock-ids: multiple IDs can be used by separating them with commas (i.e: \"minecraft:torch,minecraft:wall_torch\")"
        + "\ntemperature: the temperature of the block, in Minecraft units"
        + "\nfalloff: the block is less effective as distance increases"
        + "\nmax effect: the maximum temperature change this block can cause to a player (even with multiple blocks)"
        + "\npredicates: the state that the block has to be in for the temperature to be applied (lit=true for a campfire, for example)."
        + "\nMultiple predicates can be used by separating them with commas (i.e: \"lit=true,waterlogged=false\")");
            blockTemps = CONFIG.getString("Block Temperatures",
                                          "blocks",
                                          "[\"minecraft:fire\", 0.476, 7, true, 0.8], [\"minecraft:ice\", -0.15, 4, true, 0.5], [\"minecraft:packed_ice\", -0.25, 4, true, 1.0]",
                                          "");
            blockRange = CONFIG.getInt("Block Temperature Range",
                                       "blocks",
                                       7,
                                       1,
                                       16,
                                       "The maximum range of blocks' area of effect (Note: This will not change anything unless blocks are configured to utilize the expanded range)");

        /*
         Misc
         */
        caveInsulation = CONFIG.get("misc",
                                    "Cave Insulation",
                                    1.0,
                                    "The amount of temperature normalization from being deep underground",
                                    0.0, 1.0).getDouble();

        /*
         Seasons
         */
        if (CompatManager.isSereneSeasonsLoaded())
        {
            summerTemps = CONFIG.getString("Summer",
                                           "season_temperatures",
                                           "[0.4, 0.6, 0.4]",
                                           "");
            autumnTemps = CONFIG.getString("Autumn",
                                           "season_temperatures",
                                           "[0.2, 0.0, -0.2]",
                                           "");
            winterTemps = CONFIG.getString("Winter",
                                           "season_temperatures",
                                           "[-0.4, -0.6, -0.4]",
                                           "");
            springTemps = CONFIG.getString("Spring",
                                           "season_temperatures",
                                           "[-0.2, 0.0, 0.2]",
                                           "");
        }

        if (CONFIG.hasChanged())
        {   CONFIG.save();
        }
    }

    public static void init(String configDir)
    {
        if (configDir != null)
        {   File path = new File(configDir + "/" + ColdSweat.MOD_ID + "/world-settings.cfg");
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
