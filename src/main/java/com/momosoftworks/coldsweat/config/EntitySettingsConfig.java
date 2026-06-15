package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class EntitySettingsConfig
{
    public static Configuration CONFIG;

    public static String entityTemperatures;
    public static String insulatedEntities;

    public static void loadConfig()
    {
        /*
         Entity Temperatures (nearby mobs affect the player's temperature)
         */
        CONFIG.addCustomCategoryComment("entity_temperatures", "Defines entities that radiate temperature to nearby entities"
                                                           + "\nFormat: [[\"entity_id\", temperature, range], ...etc]"
                                                           + "\n\"entity_id\": the entity's registry name (i.e. \"minecraft:blaze\")"
                                                           + "\n\"temperature\": the temperature the entity radiates, in MC units"
                                                           + "\n\"range\": the radius (in blocks) over which the effect falls off");

        entityTemperatures = CONFIG.getString("Entity Temperatures",
                                              "entity_temperatures",
                                                      "[[\"minecraft:blaze\", 0.6, 4], "
                                                    + "[\"minecraft:snowman\", -0.4, 3]]",
                                              "");

        /*
         Insulated mounts (riding these protects from temperature)
         */
        insulatedEntities = CONFIG.getString("Insulated Entities",
                                             "mounts",
                                                     "",
                                             "Defines entities that insulate the player when ridden"
                                         + "\nFormat: [[\"entity_id\", warming, *cooling], ...etc]");

        if (CONFIG.hasChanged())
        {   CONFIG.save();
        }
    }

    public static void init(String configDir)
    {
        if (configDir != null)
        {   File path = new File(configDir + "/" + ColdSweat.MOD_ID + "/entity-settings.cfg");
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
