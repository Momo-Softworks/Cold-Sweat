package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ItemSettingsConfig
{
    public static Configuration CONFIG;

    public static String boilerItems;
    public static String iceboxItems;
    public static String hearthItems;
    public static String blacklistedPotions;
    public static boolean allowPotionsInHearth;
    public static String soulLampItems;
    public static String soulLampDimensions;
    public static String temperatureFoods;

    public static String insulatingItems;
    public static String insulationBlacklist;
    public static String adaptiveInsulatingItems;
    public static String insulatingArmor;
    public static String insulationSlots;

    public static int waterskinStrength;

    public static void loadConfig()
    {
        /*
          Fuel Items
         */
        CONFIG.addCustomCategoryComment("fuel_items", "Defines items that can be used as fuel"
                                                  + "\nFormat: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]");

        boilerItems = CONFIG.getString("Boiler",
                                       "fuel_items",
                                               "[[\"minecraft:coal\", 37], "
                                             + "[\"minecraft:charcoal\", 37], "
                                             + "[\"minecraft:coal_block\", 333], "
                                             + "[\"minecraft:magma_block\", 333], "
                                             + "[\"minecraft:lava_bucket\", 1000]]",
                                       "");

        iceboxItems = CONFIG.getString("Icebox",
                                       "fuel_items",
                                               "[[\"minecraft:snowball\", 37], "
                                             + "[\"minecraft:clay\", 37], "
                                             + "[\"minecraft:snow_block\", 333], "
                                             + "[\"minecraft:water_bucket\", 333], "
                                             + "[\"minecraft:ice\", 333], "
                                             + "[\"minecraft:packed_ice\", 1000]]",
                                       "");

        hearthItems = CONFIG.getString("Hearth",
                                       "fuel_items",
                                               "[[\"minecraft:coal\", 37], "
                                             + "[\"minecraft:charcoal\", 37], "
                                             + "[\"minecraft:coal_block\", 333], "
                                             + "[\"minecraft:magma_block\", 333], "
                                             + "[\"minecraft:lava_bucket\", 1000], "
                                             + "[\"minecraft:snowball\", -37], "
                                             + "[\"minecraft:clay\", -37], "
                                             + "[\"minecraft:snow_block\", -333], "
                                             + "[\"minecraft:water_bucket\", -333], "
                                             + "[\"minecraft:ice\", -333], "
                                             + "[\"minecraft:packed_ice\", -1000]]",
                                       "");

        blacklistedPotions = CONFIG.getString("Blacklisted Potions",
                                              "fuel_items",
                                                      "[\"minecraft:instant_damage\", "
                                                    + "\"minecraft:poison\", "
                                                    + "\"minecraft:wither\", "
                                                    + "\"minecraft:weakness\", "
                                                    + "\"minecraft:mining_fatigue\", "
                                                    + "\"minecraft:slowness\"]",
                                              "Potions containing any of these effects will not be allowed in the hearth"
                                          + "\nFormat: [\"effect_id\", \"effect_id\", ...etc]");

        allowPotionsInHearth = CONFIG.getBoolean("Allow Potions in Hearth",
                                                  "fuel_items",
                                                        true,
                                                  "If true, potions can be used as fuel in the hearth"
                                              + "\nThis gives all players in range the potion effect");

         /*
          Soulspring Lamp Items
         */
        soulLampItems = CONFIG.getString("Soulspring Lamp Items",
                                         "soulspring_lamp",
                                                "[\"cold_sweat:soul_sprout\", 4]",
                                         "Defines items that the Soulspring Lamp can use as fuel"
                                     + "\nFormat: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]");

        soulLampDimensions = CONFIG.getString("Soulspring Lamp Dimensions",
                                              "soulspring_lamp",
                                                    "[-1]",
                                              "Defines dimensions that the Soulspring Lamp can be used in"
                                          + "\nFormat: [dimension-id-1, dimension-id-2, ...etc]");

        /*
         Insulation
         */
        insulatingItems = CONFIG.getString("Insulation Ingredients",
                                           "insulation",
                                                   "[[\"minecraft:leather_helmet\", 4, 4], "
                                                 + "[\"minecraft:leather_chestplate\", 6, 6], "
                                                 + "[\"minecraft:leather_leggings\", 5, 5], "
                                                 + "[\"minecraft:leather_boots\", 4, 4], "
                                                 + "[\"minecraft:leather\", 1, 1], "
                                                 + "[\"cold_sweat:hoglin_hide\", 0, 2], "
                                                 + "[\"cold_sweat:fur\", 2, 0], "
                                                 + "[\"#minecraft:wool\", 1.5, 0], "
                                                 + "[\"minecraft:rabbit_hide\", 0, 1.5], "
                                                 + "[\"cold_sweat:hoglin_headpiece\", 0, 8], "
                                                 + "[\"cold_sweat:hoglin_tunic\", 0, 12], "
                                                 + "[\"cold_sweat:hoglin_trousers\", 0, 10], "
                                                 + "[\"cold_sweat:hoglin_hooves\", 0, 8], "
                                                 + "[\"cold_sweat:fur_cap\", 8, 0], "
                                                 + "[\"cold_sweat:fur_parka\", 12, 0], "
                                                 + "[\"cold_sweat:fur_pants\", 10, 0], "
                                                 + "[\"cold_sweat:fur_boots\", 8, 0]]",
                                           "Defines the items that can be used for insulating armor in the Sewing Table"
                                       + "\nFormat: [[\"item_id\", cold, hot], [\"item_id\", cold, hot], ...etc]"
                                       + "\n\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\")."
                                       + "\n\"cold\": The amount of cold insulation the item provides."
                                       + "\n\"hot\": The amount of heat insulation the item provides.");

        adaptiveInsulatingItems = CONFIG.getString("Adaptive Insulation Ingredients",
                                                    "insulation",
                                                            "[[\"cold_sweat:chameleon_molt\", 2, 0.0085]]",
                                                   "Defines insulation items that have the special \"chameleon molt\" effect"
                                                 + "\nFormat: [[\"item_id\", insulation, adaptSpeed], [\"item_id\", insulation, adaptSpeed], ...etc]"
                                                 + "\n\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\")."
                                                 + "\n\"insulation\": The amount of insulation the item provides. Will adjust to hot/cold based on the environment."
                                                 + "\n\"adaptSpeed\": The speed at which the item adapts to the current temperature. Higher values mean faster adaptation (from 0 to 1).");

        insulatingArmor = CONFIG.getString("Insulating Armor",
                                           "insulation",
                                                   "[[\"minecraft:leather_helmet\", 4, 4], "
                                                 + "[\"minecraft:leather_chestplate\", 6, 6], "
                                                 + "[\"minecraft:leather_leggings\", 5, 5], "
                                                 + "[\"minecraft:leather_boots\", 4, 4], "
                                                 + "[\"cold_sweat:hoglin_headpiece\", 0, 8], "
                                                 + "[\"cold_sweat:hoglin_tunic\", 0, 12], "
                                                 + "[\"cold_sweat:hoglin_trousers\", 0, 10], "
                                                 + "[\"cold_sweat:hoglin_hooves\", 0, 8], "
                                                 + "[\"cold_sweat:fur_cap\", 8, 0], "
                                                 + "[\"cold_sweat:fur_parka\", 12, 0], "
                                                 + "[\"cold_sweat:fur_pants\", 10, 0], "
                                                 + "[\"cold_sweat:fur_boots\", 8, 0]]",
                                           "Defines the items that can be used for insulating armor in the Sewing Table"
                                        + "\nFormat: [[\"item_id\", cold, hot], [\"item_id\", cold, hot], ...etc]"
                                        + "\n\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\")."
                                        + "\n\"cold\": The amount of cold insulation the item provides."
                                        + "\n\"hot\": The amount of heat insulation the item provides.");

        insulationSlots = CONFIG.getString("Insulation Slots",
                                           "insulation",
                                                "[4, 6, 5, 4]",
                                           "Defines how many insulation slots armor pieces have"
                                       + "\nFormat: [head, chest, legs, feet]");

        /*
         Consumables
         */
        temperatureFoods = CONFIG.getString("Temperature Foods",
                                            "consumables",
                                                    "",
                                            "Defines items that affect the player's temperature when consumed"
                                        + "\nFormat: [[\"item_id\", amount], [\"item_id\", amount], ...etc]");

        waterskinStrength = CONFIG.getInt("Waterskin Strength",
                                           "consumables",
                                                50,
                                           0,
                                           Integer.MAX_VALUE,
                                           "Defines the change in temperature that using a waterskin will cause");

        if (CONFIG.hasChanged())
        {   CONFIG.save();
        }
    }

    public static void init(String configDir)
    {
        if (configDir != null)
        {   File path = new File(configDir + "/" + ColdSweat.MOD_ID + "/item-settings.cfg");
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
