package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ColdSweatConfig
{
    public static Configuration CONFIG;

    public static Integer difficulty;

    public static Double maxHabitable;
    public static Double minHabitable;
    public static Double rateMultiplier;

    public static Boolean fireResistanceEffect;
    public static Boolean iceResistanceEffect;

    public static Boolean damageScaling;
    public static Boolean requireThermometer;
    public static Boolean checkSleep;

    public static Integer gracePeriodLength;
    public static Boolean gracePeriodEnabled;

    public static Double hearthEffect;
    public static String hearthSpreadWhitelist;
    public static String hearthSpreadBlacklist;

    public static Boolean  coldSoulFire;

    public static Boolean heatstrokeFog;
    public static Boolean freezingHearts;
    public static Boolean coldKnockback;
    public static Boolean coldMining;
    public static Boolean coldMovement;

    public static void loadConfig()
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        /*
         Difficulty
         */
        difficulty = CONFIG.get("general", "Difficulty", 0, "This value is used by the in-game config screen to store the current difficulty. It does nothing otherwise").getInt();

        /*
         Core temperature mechanics
         */
        minHabitable = CONFIG.get("general",
                                  "Minimum Habitable Temperature",
                                  defaultDiff.getOrDefault("min_temp", Temperature.convertUnits(50, Temperature.Units.F, Temperature.Units.MC, true)),
                                  "Defines the minimum habitable temperature").getDouble();
        maxHabitable = CONFIG.get("general",
                                  "Maximum Habitable Temperature",
                                  defaultDiff.getOrDefault("max_temp", Temperature.convertUnits(90, Temperature.Units.F, Temperature.Units.MC, true)),
                                  "Defines the maximum habitable temperature").getDouble();
        rateMultiplier = CONFIG.get("general", "Rate Multiplier", defaultDiff.getOrDefault("temp_rate", 1d), "Rate at which entities' body temperature changes").getDouble();

        /*
         Item Settings
         */
        fireResistanceEffect = CONFIG.get("item_settings",
                                          "Fire Resistance Immunity",
                                          defaultDiff.getOrDefault("fire_resistance_enabled", true),
                                          "Allow fire resistance to block overheating damage").getBoolean();
        iceResistanceEffect = CONFIG.get("item_settings",
                                         "Ice Resistance Immunity",
                                         defaultDiff.getOrDefault("ice_resistance_enabled", true),
                                         "Allow ice resistance to block freezing damage").getBoolean();
        requireThermometer = CONFIG.get("item_settings",
                                        "Require Thermometer",
                                        defaultDiff.getOrDefault("require_thermometer", true),
                                        "Thermometer item is required to see detailed world temperature").getBoolean();

        /*
         Misc. temperature settings
         */
        damageScaling = CONFIG.get("misc",
                                   "Damage Scaling",
                                   defaultDiff.getOrDefault("damage_scaling", true),
                                   "Enable damage scaling based on Minecraft's difficulty setting").getBoolean();
        checkSleep = CONFIG.get("misc",
                                "Prevent Sleep When in Danger",
                                defaultDiff.getOrDefault("check_sleep_conditions", true),
                                "Check the player's temperature when they wake up").getBoolean();
        coldSoulFire = CONFIG.get("misc",
                                  "Cold Soul Fire",
                                  true,
                                  "Converts damage dealt by Soul Fire to cold damage").getBoolean();

        /*
         Temperature effects
         */
        heatstrokeFog = CONFIG.get("Heat_Effects",
                                   "Heatstroke Fog",
                                   defaultDiff.getOrDefault("heatstroke_fog", true),
                                   "When set to true, the player's view distance will decrease when they are too hot").getBoolean();

        freezingHearts = CONFIG.get("cold_effects",
                                    "Freezing Hearts",
                                    defaultDiff.getOrDefault("freezing_hearts", true),
                                    "When set to true, some of the player's hearts will freeze when they are too cold, preventing regeneration").getBoolean();
        coldKnockback = CONFIG.get("cold_effects",
                                   "Cold Knockback Reduction",
                                   defaultDiff.getOrDefault("knockback_impairment", true),
                                   "When set to true, the player's attack knockback will be reduced when they are too cold").getBoolean();
        coldMovement = CONFIG.get("cold_effects",
                                  "Cold Slowness",
                                  defaultDiff.getOrDefault("cold_slowness", true),
                                  "When set to true, the player's movement speed will be reduced when they are too cold").getBoolean();
        coldMining = CONFIG.get("cold_effects",
                                "Cold Mining Fatigue",
                                defaultDiff.getOrDefault("cold_break_speed", true),
                                "When set to true, the player's mining speed will be reduced when they are too cold").getBoolean();

        /*
         Grace Period
         */
        gracePeriodEnabled = CONFIG.get("grace_period",
                                        "Enable Grace Period",
                                        true,
                                        "When set to true, the player will be immune to temperature effects for a period after spawning").getBoolean();
        gracePeriodLength = CONFIG.getInt("Grace Period Length",
                                          "grace_period",
                                          defaultDiff.getOrDefault("grace_length", 6000), 0, Integer.MAX_VALUE,
                                          "The number of ticks for which the grace period will last");

        /*
         Hearth
         */
        hearthEffect = CONFIG.get("hearth",
                                  "Hearth Effect",
                                  defaultDiff.getOrDefault("hearth_effect", 0.5),
                                  "The strength of the hearth's effect on temperature", 0, 1).getDouble();

        hearthSpreadWhitelist = CONFIG.get("hearth",
                                           "Hearth Spread Whitelist",
                                           "",
                                           "A comma-separated list of block IDs that the hearth can spread through").getString();
        hearthSpreadBlacklist = CONFIG.get("hearth",
                                           "Hearth Spread Blacklist",
                                           "",
                                           "A comma-separated list of block IDs that the hearth cannot spread through").getString();

        if (CONFIG.hasChanged())
        {   CONFIG.save();
        }
    }

    public static void init(String configDir)
    {
        if (configDir != null)
        {   File path = new File(configDir + "/" + ColdSweat.MOD_ID + "/main.cfg");
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
