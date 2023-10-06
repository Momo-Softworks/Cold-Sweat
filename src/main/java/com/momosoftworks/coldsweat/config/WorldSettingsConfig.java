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
        + "\nbiome: The ID of the biome. Can be either an integer (i.e: 1 for Plains) or a string (i.e: \"Plains\"). "
        + "\ntemp-low: The temperature of the biome at midnight"
        + "\ntemp-high: The temperature of the biome at noon"
        + "\nunits: Optional. The units of the given temperature(s) (\"C\", \"F\", or \"MC\". Defaults to MC units)");
            biomeOffsets = CONFIG.getString("Biome Temperature Offsets",
                                            "biomes",
                                                    "",
                                            "Applies an offset to the temperature of a biome");
            biomeTemps = CONFIG.getString("Biome Temperatures",
                                          "biomes",
                                                "[[Jungle, 76, 87, F], "
                                              + "[Desert, 48, 115, F], "
                                              + "[DesertHills, 48, 115, F], "
                                              + "[Desert M, 48, 115, F], "
                                              + "[River, 60, 70, F], "
                                              + "[Swampland, 72, 84, F], "
                                              + "[Swampland M, 72, 84, F], "
                                              + "[Taiga, 44, 62, F], "
                                              + "[TaigaHills, 44, 62, F], "
                                              + "[Cold Taiga, 19, 48, F], "
                                              + "[Cold Taiga Hills, 19, 48, F], "
                                              + "[Cold Taiga M, 19, 48, F], "
                                              + "[Mega Taiga, 48, 62, F], "
                                              + "[Mega Taiga Hills, 43, 58, F], "
                                              + "[Mega Spruce Taiga, 48, 62, F], "
                                              + "[Birch Forest M, 58, 72, F], "
                                              + "[Birch Forest Hills M, 58, 72, F], "
                                              + "[Savanna, 70, 95, F], "
                                              + "[Savanna M, 67, 90, F], "
                                              + "[Savanna Plateau, 76, 98, F], "
                                              + "[Savanna Plateau M, 67, 90, F], "
                                              + "[Stone Beach, 50, 64, F], "
                                              + "[Extreme Hills, 48, 66, F], "
                                              + "[Extreme Hills+, 48, 66, F], "
                                              + "[Extreme Hills+ M, 28, 48, F], "
                                              + "[Extreme Hills M, 28, 48, F], "
                                              + "[Ice Mountains, 15, 33, F], "
                                              + "[Jungle, 76, 87, F], "
                                              + "[JungleEdge, 68, 82, F], "
                                              + "[JungleEdge M, 68, 82, F], "
                                              + "[JungleEdge M, 68, 82, F], "
                                              + "[Jungle M, 76, 87, F]"
                                              + "[Mesa, 84, 120, F]"
                                              + "[Mesa Plateau F, 84, 120, F]"
                                              + "[Mesa Plateau M, 84, 120, F]]",
                                          "Defines the temperature of a biome, overriding existing biome temperatures & offsets");

        /*
         Block Temperatures
         */
        CONFIG.addCustomCategoryComment("blocks", "Format: [\"block-ids\", <temperature>, <range (max 7)>, <*true/false: falloff>, <*max effect>, <*meta_values>], [etc...], [etc...]"
        + "\n(* = optional) (1 \u00B0MC = 42 \u00B0F/ 23.33 \u00B0C)"
        + "\nArguments:"
        + "\nblock-ids: multiple IDs can be used by separating them with commas (i.e: minecraft:torch,minecraft:wall_torch)"
        + "\ntemperature: the temperature of the block, in Minecraft units"
        + "\nfalloff: the block is less effective as distance increases"
        + "\nmax effect: the maximum temperature change this block can cause to a player (even with multiple blocks)"
        + "\npredicates: the state that the block has to be in for the temperature to be applied (lit=true for a campfire, for example)."
        + "\nMultiple predicates can be used by separating them with commas (i.e: \"lit=true,waterlogged=false\")");
            blockTemps = CONFIG.getString("Block Temperatures",
                                          "blocks",
                                          "[minecraft:fire, 0.476, 7, true, 0.8], [minecraft:ice, -0.15, 4, true, 0.5], [minecraft:packed_ice, -0.25, 4, true, 1.0]",
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
