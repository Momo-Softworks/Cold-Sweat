package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class ItemSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> boilerItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> iceboxItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> hearthItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedPotions;
    private static final ForgeConfigSpec.BooleanValue allowPotionsInHearth;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> soulLampItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> soulLampDimensions;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> temperatureFoods;

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> insulatingItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> insulationBlacklist;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> adaptiveInsulatingItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> insulatingArmor;
    private static final ForgeConfigSpec.ConfigValue<List<? extends Number>> insulationSlots;

    private static final ForgeConfigSpec.IntValue waterskinStrength;

    private static ForgeConfigSpec.ConfigValue<List<? extends List<?>>> insulatingCurios;

    static final ItemSettingsConfig INSTANCE = new ItemSettingsConfig();

    static
    {
        /*
          Fuel Items
         */
        BUILDER.push("Fuel Items")
                .comment("Defines items that can be used as fuel",
                         "Format: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]");
        boilerItems = BUILDER
                .defineListAllowEmpty(List.of("Boiler"), () -> ListBuilder.begin(
                                List.of("#minecraft:planks",         10),
                                List.of("minecraft:coal",            37),
                                List.of("minecraft:charcoal",        37),
                                List.of("#minecraft:logs_that_burn", 37),
                                List.of("minecraft:coal_block",      333),
                                List.of("minecraft:magma_block",     333),
                                List.of("minecraft:lava_bucket",     1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        iceboxItems = BUILDER
                .defineListAllowEmpty(List.of("Icebox"), () -> ListBuilder.begin(
                                List.of("minecraft:snowball",           37),
                                List.of("minecraft:clay_ball",          37),
                                List.of("minecraft:snow_block",         333),
                                List.of("minecraft:ice",                333),
                                List.of("minecraft:clay",               333),
                                List.of("minecraft:powder_snow_bucket", 333),
                                List.of("minecraft:water_bucket",       1000),
                                List.of("minecraft:packed_ice",         1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        hearthItems = BUILDER
                .comment("Negative values indicate cold fuel")
                .defineListAllowEmpty(List.of("Hearth"), () -> ListBuilder.begin(
                                // Hot
                                List.of("#minecraft:planks",         10),
                                List.of("minecraft:coal",            37),
                                List.of("minecraft:charcoal",        37),
                                List.of("#minecraft:logs_that_burn", 37),
                                List.of("minecraft:coal_block",      333),
                                List.of("minecraft:magma_block",     333),
                                List.of("minecraft:lava_bucket",     1000),
                                // Cold
                                List.of("minecraft:snowball",           -37),
                                List.of("minecraft:clay_ball",          -37),
                                List.of("minecraft:snow_block",         -333),
                                List.of("minecraft:ice",                -333),
                                List.of("minecraft:clay",               -333),
                                List.of("minecraft:powder_snow_bucket", -333),
                                List.of("minecraft:water_bucket",       -1000),
                                List.of("minecraft:packed_ice",         -1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);
        blacklistedPotions = BUILDER
                .comment("Potions containing any of these effects will not be allowed in the hearth",
                         "Format: [\"effect_id\", \"effect_id\", ...etc]")
                .defineListAllowEmpty(List.of("Blacklisted Hearth Potions"), () -> ListBuilder.begin(
                                "minecraft:instant_damage",
                                "minecraft:poison",
                                "minecraft:wither",
                                "minecraft:weakness",
                                "minecraft:mining_fatigue",
                                "minecraft:slowness"
                        ).build(),
                        it -> it instanceof String);
        allowPotionsInHearth = BUILDER
                .comment("If true, potions can be used as fuel in the hearth",
                         "This gives all players in range the potion effect")
                .define("Allow Potions in Hearth", true);
        BUILDER.pop();

        /*
          Soulspring Lamp Items
         */
        BUILDER.push("Soulspring Lamp");
        soulLampItems = BUILDER
                .comment("Defines items that the Soulspring Lamp can use as fuel",
                        "Format: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]")
                .defineListAllowEmpty(List.of("Fuel Items"), () -> ListBuilder.begin(
                                    List.of("cold_sweat:soul_sprout", 4)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

        soulLampDimensions = BUILDER
                .comment("Defines the dimensions that the Soulspring Lamp can be used in",
                        "Format: [\"dimension-id-1\", \"dimension-id-2\", ...etc]")
                .defineListAllowEmpty(List.of("Valid Dimensions"), () -> ListBuilder.begin(
                                "minecraft:the_nether"
                        ).build(),
                        it -> it instanceof String);
        BUILDER.pop();

        /*
         Insulation
         */
        BUILDER.push("Insulation");
        insulatingItems = BUILDER
                .comment("Defines the items that can be used for insulating armor in the Sewing Table",
                        "Format: [[\"item_id\", cold, hot], [\"item_id\", cold, hot], ...etc]",
                        "\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\").",
                        "\"cold\": The amount of cold insulation the item provides.",
                        "\"hot\": The amount of heat insulation the item provides.")
                .defineListAllowEmpty(List.of("Insulation Ingredients"), () -> ListBuilder.begin(
                                List.of("minecraft:leather_helmet",     4,  4),
                                List.of("minecraft:leather_chestplate", 6,  6),
                                List.of("minecraft:leather_leggings",   5,  5),
                                List.of("minecraft:leather_boots",      4,  4),
                                List.of("minecraft:leather",            1,  1),
                                List.of("cold_sweat:hoglin_hide",       0,  2),
                                List.of("cold_sweat:fur",          2,  0),
                                List.of("#minecraft:wool",             1.5, 0),
                                List.of("minecraft:rabbit_hide",        0,  1.5),
                                List.of("cold_sweat:hoglin_headpiece",  0,  8),
                                List.of("cold_sweat:hoglin_tunic",      0,  12),
                                List.of("cold_sweat:hoglin_trousers",   0,  10),
                                List.of("cold_sweat:hoglin_hooves",     0,  8),
                                List.of("cold_sweat:fur_cap",      8,  0),
                                List.of("cold_sweat:fur_parka",    12, 0),
                                List.of("cold_sweat:fur_pants",    10, 0),
                                List.of("cold_sweat:fur_boots",    8,  0))
                            .addIf(CompatManager.isEnvironmentalLoaded(),
                                () -> List.of("environmental:yak_hair", 1.5, -1)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 3 && list.get(0) instanceof String && list.get(1) instanceof Number && list.get(2) instanceof Number);

         adaptiveInsulatingItems = BUILDER
                .comment("Defines insulation items that have the special \"chameleon molt\" effect",
                         "Format: [[\"item_id\", insulation, adaptSpeed], [\"item_id\", insulation, adaptSpeed], ...etc]",
                        "\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\").",
                        "\"insulation\": The amount of insulation the item provides. Will adjust to hot/cold based on the environment.",
                        "\"adaptSpeed\": The speed at which the item adapts to the current temperature. Higher values mean faster adaptation (from 0 to 1).")
                 .defineListAllowEmpty(List.of("Adaptive Insulation Ingredients"), () -> ListBuilder.begin(
                                List.of("cold_sweat:chameleon_molt", 2, 0.0085)
                            ).build(),
                        it -> it instanceof List<?> list && list.size() == 3 && list.get(0) instanceof String && list.get(1) instanceof Number && list.get(2) instanceof Number);

        insulatingArmor = BUILDER
                .comment("Defines the items that provide insulation when worn",
                        "Format: [[\"item_id\", cold, hot], [\"item_id\", cold, hot], ...etc]",
                         "\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\").",
                         "\"cold\": The amount of cold insulation the item provides.",
                         "\"hot\": The amount of heat insulation the item provides.")
                .defineListAllowEmpty(List.of("Insulating Armor"), () -> ListBuilder.begin(
                                List.of("minecraft:leather_helmet",      4,  4),
                                List.of("minecraft:leather_chestplate",  6,  6),
                                List.of("minecraft:leather_leggings",    5,  5),
                                List.of("minecraft:leather_boots",       4,  4),
                                List.of("cold_sweat:hoglin_headpiece",   0,  8),
                                List.of("cold_sweat:hoglin_tunic",       0,  12),
                                List.of("cold_sweat:hoglin_trousers",    0,  10),
                                List.of("cold_sweat:hoglin_hooves",      0,  8),
                                List.of("cold_sweat:fur_cap",       8,  0),
                                List.of("cold_sweat:fur_parka",     12, 0),
                                List.of("cold_sweat:fur_pants",     10, 0),
                                List.of("cold_sweat:fur_boots",     8,  0))
                            .addIf(CompatManager.isEnvironmentalLoaded(),
                                () -> List.of("environmental:yak_pants", 7.5, -5)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 3 && list.get(0) instanceof String && list.get(1) instanceof Number && list.get(2) instanceof Number);

        if (CompatManager.isCuriosLoaded())
        {
            insulatingCurios = BUILDER
                    .comment("Defines the items that provide insulation when worn in a curio slot",
                             "Format: [[\"item_id\", cold, hot], [\"item_id\", cold, hot], ...etc]",
                             "\"item_id\": The item's ID (i.e. \"minecraft:iron_ingot\"). Accepts tags with \"#\" (i.e. \"#minecraft:wool\").",
                             "\"cold\": The amount of cold insulation the item provides.",
                             "\"hot\": The amount of heat insulation the item provides.")
                    .defineListAllowEmpty(List.of("Insulating Curios"), () -> List.of(),
                                          it -> it instanceof List<?> list && list.size() == 3 && list.get(0) instanceof String && list.get(1) instanceof Number && list.get(2) instanceof Number);
        }

        insulationSlots = BUILDER
                .comment("Defines how many insulation slots armor pieces have",
                         "Format: [head, chest, legs, feet]")
                .defineList("Insulation Slots", List.of(4, 6, 5, 4),
                        it -> it instanceof Number);

        insulationBlacklist = BUILDER
                .comment("Defines wearable items that cannot be insulated",
                        "Format: [\"item_id\", \"item_id\", ...etc]")
                .defineListAllowEmpty(List.of("Insulation Blacklist"), () -> List.of(
                ),
                it -> it instanceof String);

        BUILDER.pop();

        /*
         Consumables
         */
        BUILDER.push("Consumables");
        temperatureFoods = BUILDER
                .comment("Defines items that affect the player's temperature when consumed",
                        "Format: [[\"item_id\", amount], [\"item_id\", amount], ...etc]",
                        "Negative values are cold foods, positive values are hot foods")
                .defineListAllowEmpty(List.of("Temperature-Affecting Foods"), () -> Arrays.asList(
                ),
                it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        waterskinStrength = BUILDER
                .comment("Defines how much a waterskin will change the player's body temperature by when used")
                .defineInRange("Waterskin Strength", 50, 0, Integer.MAX_VALUE);
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

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/item_settings.toml");
    }

    public static ItemSettingsConfig getInstance()
    {   return INSTANCE;
    }

    public List<? extends List<?>> getBoilerFuelItems()
    {   return boilerItems.get();
    }

    public List<? extends List<?>> getIceboxFuelItems()
    {   return iceboxItems.get();
    }

    public List<? extends List<?>> getHearthFuelItems()
    {   return hearthItems.get();
    }

    public List<? extends List<?>> getInsulationItems()
    {   return insulatingItems.get();
    }

    public List<? extends List<?>> getAdaptiveInsulationItems()
    {   return adaptiveInsulatingItems.get();
    }

    public List<? extends List<?>> getInsulatingArmorItems()
    {   return insulatingArmor.get();
    }

    public List<? extends Number> getArmorInsulationSlots()
    {   return insulationSlots.get();
    }

    public List<? extends String> getInsulationBlacklist()
    {   return insulationBlacklist.get();
    }

    public List<? extends List<?>> getSoulLampFuelItems()
    {   return soulLampItems.get();
    }

    public List<? extends List<?>> getFoodTemperatures()
    {   return temperatureFoods.get();
    }

    public List<? extends String> getValidSoulLampDimensions()
    {   return soulLampDimensions.get();
    }

    public int getWaterskinStrength()
    {   return waterskinStrength.get();
    }

    public boolean arePotionsEnabled()
    {   return allowPotionsInHearth.get();
    }

    public List<String> getPotionBlacklist()
    {   return (List<String>) blacklistedPotions.get();
    }

    public List<? extends List<?>> getInsulatingCurios()
    {   return CompatManager.isCuriosLoaded() ? insulatingCurios.get() : List.of();
    }

    public void setBoilerFuelItems(List<? extends List<?>> itemMap)
    {   boilerItems.set(itemMap);
    }

    public void setIceboxFuelItems(List<? extends List<?>> itemMap)
    {   iceboxItems.set(itemMap);
    }

    public void setHearthFuelItems(List<? extends List<?>> itemMap)
    {   hearthItems.set(itemMap);
    }

    public void setInsulationItems(List<? extends List<?>> items)
    {   insulatingItems.set(items);
    }

    public void setAdaptiveInsulationItems(List<? extends List<?>> items)
    {   adaptiveInsulatingItems.set(items);
    }

    public void setInsulatingArmorItems(List<? extends List<?>> itemMap)
    {   insulatingArmor.set(itemMap);
    }

    public void setArmorInsulationSlots(List<? extends Number> slots)
    {   insulationSlots.set(slots);
    }

    public void setSoulLampFuelItems(List<? extends List<?>> items)
    {   soulLampItems.set(items);
    }

    public void setFoodTemperatures(List<? extends List<?>> itemMap)
    {   temperatureFoods.set(itemMap);
    }

    public void setValidSoulLampDimensions(List<? extends String> items)
    {   soulLampDimensions.set(items);
    }

    public void setWaterskinStrength(int strength)
    {   waterskinStrength.set(strength);
    }

    public void setPotionsEnabled(Boolean saver)
    {   allowPotionsInHearth.set(saver);
    }

    public void setPotionBlacklist(List<String> saver)
    {   blacklistedPotions.set(saver);
    }

    public void setInsulationBlacklist(List<String> blacklist)
    {   insulationBlacklist.set(blacklist);
    }

    public void setInsulatingCurios(List<? extends List<?>> items)
    {
        if (CompatManager.isCuriosLoaded())
        {   insulatingCurios.set(items);
        }
    }
}
