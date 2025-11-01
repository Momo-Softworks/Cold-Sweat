package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ItemSettingsConfig
{
    private static final CSConfigSpec SPEC;
    private static final CSConfigSpec.Builder BUILDER = new CSConfigSpec.Builder();

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> BOILER_FUELS;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> ICEBOX_FUELS;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> HEARTH_FUELS;
    public static final CSConfigSpec.ConfigValue<List<? extends String>> HEARTH_POTION_BLACKLIST;
    public static final CSConfigSpec.BooleanValue ALLOW_POTIONS_IN_HEARTH;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> SOULSPRING_LAMP_FUELS;
    public static final CSConfigSpec.ConfigValue<List<? extends String>> SOULSPRING_LAMP_DIMENSIONS;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_TEMPERATURES;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> ITEM_TEMPERATURES;

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> INSULATION_ITEMS;
    public static final CSConfigSpec.ConfigValue<List<? extends String>> INSULATION_BLACKLIST;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> INSULATING_ARMOR;
    public static final CSConfigSpec.ConfigValue<List<?>> INSULATION_SLOTS;
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> INSULATION_SLOT_OVERRIDES;
    public static final CSConfigSpec.DoubleValue INSULATION_STRENGTH;

    public static final CSConfigSpec.IntValue WATERSKIN_CONSUME_STRENGTH;
    public static final CSConfigSpec.DoubleValue WATERSKIN_HOTBAR_STRENGTH;
    public static final CSConfigSpec.DoubleValue WATERSKIN_NEUTRALIZE_SPEED;
    public static final CSConfigSpec.IntValue WATERSKIN_USES;
    public static final CSConfigSpec.DoubleValue SOULSPRING_LAMP_STRENGTH;

    public static final CSConfigSpec.ConfigValue<Boolean> FIRE_RESISTANCE_BLOCKS_OVERHEATING;
    public static final CSConfigSpec.ConfigValue<Boolean> ICE_RESISTANCE_BLOCKS_FREEZING;
    public static final CSConfigSpec.ConfigValue<Boolean> REQUIRE_THERMOMETER;

    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> DRYING_ITEMS;

    // Compat settings
    public static final CSConfigSpec.ConfigValue<List<? extends List<?>>> INSULATING_CURIOS;
    public static final CSConfigSpec.BooleanValue HEAT_DRAINS_BACKTANK;
    public static final CSConfigSpec.BooleanValue COLD_DRAINS_BACKTANK;

    static
    {
        ConfigSettings.Difficulty defaultDiff = ConfigSettings.DEFAULT_DIFFICULTY;

        BUILDER.comment("─────────────────────────────────────────────────────────────────────────",
                        " Anywhere that uses item IDs also supports:",
                        " • Tags (e.g. \"#minecraft:planks\")",
                        " • Comma-separated lists (e.g. \"minecraft:blaze_powder,#forge:rods/blaze\")",
                        "     Applies the setting to all listed IDs. Can use tags, regular IDs, and negation interchangeably",
                        " • Negation (e.g. \"!minecraft:black_dye\")",
                        "     Useful with lists/tags. Excludes the listed IDs from the setting",
                        "     i.e. \"#forge:dyes,!minecraft:black_dye\" (all dyes EXCEPT black dye)",
                        " Settings with \"//v\" will list elements vertically. Removing \"//v\" will list elements in one line",
                        "─────────────────────────────────────────────────────────────────────────");

        BUILDER.comment("─────────────────────────────────────────────────────────────────────────",
                        " Defines items that can be used as fuel",
                        " └── Format: [[\"item_id\", amount], [...], etc]",
                        " • item_id: The item's ID (i.e. \"minecraft:coal\").",
                        " • amount: The amount of fuel the item provides. Higher values mean the item burns longer",
                        " ⌄ ")
               .push("Fuel Items");
            BOILER_FUELS = BUILDER
                .comment("─────────────────────────────────//v")
                .defineListAllowEmpty(List.of("Boiler"), () -> ListBuilder.begin(
                                List.of("#minecraft:planks",         10),
                                List.of("#minecraft:coals",          37),
                                List.of("#minecraft:logs_that_burn", 37),
                                List.of("minecraft:dried_kelp_block", 92),
                                List.of("minecraft:coal_block",      333),
                                List.of("minecraft:magma_block",     333),
                                List.of("minecraft:lava_bucket",     1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

            ICEBOX_FUELS = BUILDER
                .comment("─────────────────────────────────//v")
                .defineListAllowEmpty(List.of("Icebox"), () -> ListBuilder.begin(
                                List.of("minecraft:snowball",           10),
                                List.of("minecraft:clay_ball",          37),
                                List.of("minecraft:snow_block",         100),
                                List.of("minecraft:ice",                250),
                                List.of("minecraft:clay",               333),
                                List.of("minecraft:powder_snow_bucket", 100),
                                List.of("minecraft:packed_ice",         1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

            HEARTH_FUELS = BUILDER
                .comment("─────────────────────────────────//v",
                         " (Negative values indicate cold fuel")
                .defineListAllowEmpty(List.of("Hearth"), () -> ListBuilder.begin(
                                // Hot
                                List.of("#minecraft:planks",         10),
                                List.of("#minecraft:coals",          37),
                                List.of("#minecraft:logs_that_burn", 37),
                                List.of("minecraft:dried_kelp_block", 92),
                                List.of("minecraft:coal_block",      333),
                                List.of("minecraft:magma_block",     333),
                                List.of("minecraft:lava_bucket",     1000),
                                // Cold
                                List.of("minecraft:snowball",           -10),
                                List.of("minecraft:clay_ball",          -37),
                                List.of("minecraft:snow_block",         -100),
                                List.of("minecraft:ice",                -250),
                                List.of("minecraft:clay",               -333),
                                List.of("minecraft:powder_snow_bucket", -100),
                                List.of("minecraft:packed_ice",         -1000)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);
        BUILDER.pop();


        BUILDER.push("Hearth Extras");

            HEARTH_POTION_BLACKLIST = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Potions containing any of these effects will not be allowed in the hearth",
                         " └── Format: [\"effect_id\", \"effect_id\", ...etc]",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Blacklisted Hearth Potions"), () -> ListBuilder.begin(
                                              "minecraft:instant_damage",
                                              "minecraft:poison",
                                              "minecraft:wither",
                                              "minecraft:weakness",
                                              "minecraft:mining_fatigue",
                                              "minecraft:slowness"
                                      ).build(),
                                      it -> it instanceof String);
            ALLOW_POTIONS_IN_HEARTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " If true, potions can be used in the hearth to give all players in range the potion effect",
                         " ⌄ ")
                .define("Allow Potions in Hearth", true);

        BUILDER.pop();


        BUILDER.push("Soulspring Lamp");
            SOULSPRING_LAMP_FUELS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines items that the Soulspring Lamp can use as fuel",
                         " └── Format: [[\"item_id\", amount], [...], etc]",
                         " • item_id: The item's ID (i.e. \"cold_sweat:soul_sprout\").",
                         " • amount: The amount of fuel the item provides. Higher values mean the item burns longer",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Fuel Items"), () -> ListBuilder.<List<?>>begin(
                                    List.of("cold_sweat:soul_sprout", 4)
                        ).build(),
                        it -> it instanceof List<?> list && list.size() == 2 && list.get(0) instanceof String && list.get(1) instanceof Number);

            SOULSPRING_LAMP_DIMENSIONS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the dimensions that the Soulspring Lamp can be used in",
                         " └── Format: [\"dimension_id\", \"dimension_id\", ...etc]",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Valid Dimensions"), () -> ListBuilder.begin(
                                "minecraft:the_nether"
                        ).build(),
                        it -> it instanceof String);
        BUILDER.pop();

        /*
         Insulation
         */
        BUILDER.push("Insulation");
            INSULATION_ITEMS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the items that can be used for insulating armor in the Sewing Table",
                         " ├── Format (Static): [[\"item_id\", cold, heat, *\"static\", *\"{nbt}\", *fillSlots], [...], etc]",
                         " ├── Format (Adaptive): [[\"item_id\", amount, adaptSpeed, \"adaptive\", *\"{nbt}\", *fillSlots], [...], etc]",
                         " └── [* = optional]",
                         " • item_id: The item's ID (i.e. \"minecraft:iron_ingot\").",
                         " • cold: The cold insulation the item provides.",
                         " • heat: The heat insulation the item provides.",
                         " • amount: The amount of insulation the item provides.",
                         " • adaptSpeed: The speed at which the insulation adapts to the environment.",
                         " • *static/adaptive: The type of insulation the item provides. Defaults to \"static\" if unset",
                         " • *nbt: If set, the item will only provide insulation if it has the specified NBT tag.",
                         " • *fillSlots: If true, the item will fill 1 slot per 2 insulation points. Otherwise, the item will fill 1 slot.",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Insulation Ingredients"), () -> ListBuilder.<List<?>>begin(
                                List.of("minecraft:leather",            1,  1),
                                List.of("cold_sweat:chameleon_molt",    2, 0.0085, "adaptive"),
                                List.of("cold_sweat:hoglin_hide",       0,  2),
                                List.of("cold_sweat:goat_fur",          2,  0),
                                List.of("#minecraft:wool",              1.5, 0),
                                List.of("minecraft:rabbit_hide",        0,  1.5))
                            .build(),
                        it -> it instanceof List<?> list && list.size() >= 3
                                && list.get(0) instanceof String
                                && (list.get(1) instanceof Number || list.get(1) instanceof List<?>)
                                && (list.get(2) instanceof Number || list.get(2) instanceof List<?>)
                                && (list.size() < 4 || list.get(3) instanceof String)
                                && (list.size() < 5 || list.get(4) instanceof String)
                                && (list.size() < 6 || list.get(5) instanceof Boolean));

            INSULATING_ARMOR = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines the items that provide insulation when worn",
                         " ** See Insulation Ingredients for formatting.",
                         " Enabling \"fillSlots\" will cause the armor's built-in insulation to consume the available insulation slots",
                         " i.e. if an item has 5 insulation slots and gives one slot of cold insulation by default, it will only have 4 free slots left",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Insulating Armor"), () -> ListBuilder.<List<?>>begin(
                                List.of("minecraft:leather_helmet",      5,  5),
                                List.of("minecraft:leather_chestplate",  7,  7),
                                List.of("minecraft:leather_leggings",    6,  6),
                                List.of("minecraft:leather_boots",       5,  5),

                                List.of("cold_sweat:hoglin_helmet",   0,  10),
                                List.of("cold_sweat:hoglin_chestplate",       0,  14),
                                List.of("cold_sweat:hoglin_leggings",    0,  12),
                                List.of("cold_sweat:hoglin_boots",      0,  10),

                                List.of("cold_sweat:goat_fur_helmet",       10, 0),
                                List.of("cold_sweat:goat_fur_chestplate",     14, 0),
                                List.of("cold_sweat:goat_fur_leggings",     12, 0),
                                List.of("cold_sweat:goat_fur_boots",     10, 0),

                                List.of("cold_sweat:chameleon_helmet",     10, 0.0085, "adaptive"),
                                List.of("cold_sweat:chameleon_chestplate", 14, 0.0085, "adaptive"),
                                List.of("cold_sweat:chameleon_leggings",   12, 0.0085, "adaptive"),
                                List.of("cold_sweat:chameleon_boots",      10, 0.0085, "adaptive"))
                            .addIf(CompatManager.isToughAsNailsLoaded(),
                                () -> List.of("toughasnails:leaf_helmet",     0, List.of(1, 1, 1, 1, 1)),
                                () -> List.of("toughasnails:leaf_chestplate", 0, List.of(1, 1, 1, 1, 1, 1, 1)),
                                () -> List.of("toughasnails:leaf_leggings",   0, List.of(1, 1, 1, 1, 1, 1)),
                                () -> List.of("toughasnails:leaf_boots",      0, List.of(1, 1, 1, 1, 1)),
                                () -> List.of("toughasnails:wool_helmet",     List.of(1.5, 1.5, 1.5, 1.5, 1.5), 0),
                                () -> List.of("toughasnails:wool_chestplate", List.of(1.5, 1.5, 1.5, 1.5, 1.5, 1.5, 1.5), 0),
                                () -> List.of("toughasnails:wool_leggings",   List.of(1.5, 1.5, 1.5, 1.5, 1.5, 1.5), 0),
                                () -> List.of("toughasnails:wool_boots",      List.of(1.5, 1.5, 1.5, 1.5, 1.5), 0))
                            .build(),
                        it -> it instanceof List<?> list && list.size() >= 3
                                && list.get(0) instanceof String
                                && (list.get(1) instanceof Number || list.get(1) instanceof List<?>)
                                && (list.get(2) instanceof Number || list.get(2) instanceof List<?>)
                                && (list.size() < 4 || list.get(3) instanceof String)
                                && (list.size() < 5 || list.get(4) instanceof String)
                                && (list.size() < 6 || list.get(5) instanceof Boolean));

            if (CompatManager.isCuriosLoaded())
            {
                INSULATING_CURIOS = BUILDER
                        .comment("─────────────────────────────────────────────────────────────────────────//v",
                                 " Defines the items that provide insulation when worn in a curio slot",
                                 " See Insulation Ingredients for formatting. This setting does not have a \"fillSlots\" option",
                                 " ⌄ ")
                        .defineListAllowEmpty(List.of("Insulating Curios"), () -> List.of(
                                // Nothing defined
                            ),
                            it -> it instanceof List<?> list && list.size() >= 3
                                    && list.get(0) instanceof String
                                    && list.get(1) instanceof Number
                                    && list.get(2) instanceof Number
                                    && (list.size() < 4 || list.get(3) instanceof String)
                                    && (list.size() < 5 || list.get(4) instanceof String));
            }
            else INSULATING_CURIOS = null;

            INSULATION_SLOTS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how many insulation slots armor pieces have",
                         " There are 4 modes for this setting:",
                         " ┌ Static: Each armor slot (head, body, legs, feet) has a fixed number of insulation slots",
                         " └─── Format: [head, body, legs, feet]",
                         "      • head/body/legs/feet: The number of insulation slots for that armor slot",
                         " ┌ Linear: Number of slots increases steadily with protection",
                         " ├ Exponential: Number of slots increases exponentially with protection",
                         " ├ Logarithmic: Number of slots increases with protection, with diminishing returns",
                         " └─── Format: [multiplier, max-slots]",
                         "      • multiplier: Multiplied by the armor's protection value to get the number of insulation slots",
                         "      • max-slots: The maximum number of insulation slots an armor piece can have",
                         " ⌄ ")
                .defineList("Insulation Slots", List.of("static", 4, 6, 5, 4),
                        it -> it instanceof Number || it instanceof String);

            INSULATION_SLOT_OVERRIDES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Allows for overriding the number of insulation slots for specific items",
                         " └── Format: [[\"item_id\", slotCount, *\"{nbt}\"], [\"item_id\", slotCount, *\"{nbt}\"], [...], etc]",
                         " • item_id: The item's ID (i.e. \"minecraft:iron_helmet\").",
                         " • slot_count: The number of insulation slots the item should have.",
                         " • *nbt: If set, the item will only have the specified number of insulation slots if it has the specified NBT tag.",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Insulation Slot Overrides"), () -> List.of(
                ),
                it -> it instanceof List<?> list && list.size() == 2
                        && list.get(0) instanceof String
                        && list.get(1) instanceof Number
                        && (list.size() < 3 || list.get(2) instanceof String));

            INSULATION_STRENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines the effectiveness of insulating items in protecting against temperature",
                         " ⌄ ")
                .defineInRange("Insulation Strength", 1.0, 0, Double.POSITIVE_INFINITY);

            INSULATION_BLACKLIST = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines wearable items that cannot be insulated",
                         " └── Format: [\"item_id\", \"item_id\", ...etc]",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Insulation Blacklist"), () -> List.of(
                ),
                it -> it instanceof String);

        BUILDER.pop();

        /*
         Consumables
         */
        BUILDER.push("Consumables");

            FOOD_TEMPERATURES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines items that affect the player's temperature when consumed",
                         " ├── Format: [[\"item_id\", amount, *\"{nbt}\", *duration], [\"item_id\", amount, *\"{nbt}\", *duration], ...etc]",
                         " └── [* = optional]",
                         " • item_id: The item's ID (i.e. \"minecraft:apple\").",
                         " • amount: The amount to change the player's temperature by. Negative values are cold, positive values are hot",
                         " • *nbt: If set, the item will only affect the player's temperature if it has the specified NBT tag.",
                         " • *duration: If set, the player's temperature will remain increased/decreased for this amount of time (in ticks).",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Temperature-Affecting Foods"), () -> Arrays.asList(
                        List.of("cold_sweat:soul_sprout", -20, "{}", 1200)
                ),
                it -> it instanceof List<?> list && list.size() >= 2
                        && list.get(0) instanceof String
                        && list.get(1) instanceof Number
                        && (list.size() < 3 || list.get(2) instanceof String)
                        && (list.size() < 4 || list.get(3) instanceof Number));

            WATERSKIN_CONSUME_STRENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how much a waterskin will change the player's body temperature when used",
                         " ⌄ ")
                .defineInRange("Waterskin Strength", 50, 0, Integer.MAX_VALUE);

            WATERSKIN_HOTBAR_STRENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " A multiplier for how effective a waterskin's over-time effect is, when held in the player's hotbar",
                         " ⌄ ")
                .defineInRange("Waterskin Hotbar Strength", 1.0, 0, Double.POSITIVE_INFINITY);

            WATERSKIN_NEUTRALIZE_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " A multiplier for how quickly a waterskin will return to its neutral temperature when being used in the hotbar",
                         " ⌄ ")
                .defineInRange("Waterskin Neutralize Speed", 1.0, 0, Double.POSITIVE_INFINITY);

            WATERSKIN_USES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Defines how many times a waterskin can be used before becoming empty",
                         " ⌄ ")
                .defineInRange("Waterskin Uses", 1, 1, Integer.MAX_VALUE);

            SOULSPRING_LAMP_STRENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Determines the strength of the Soulspring Lamp's effect before it is overwhelmed",
                         " A value of 1 means it will never be overwhelmed",
                         " ⌄ ")
                .defineInRange("Soulspring Lamp Strength", 0.6, 0, 1);

            DRYING_ITEMS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines items that can be used to dry the player",
                         " └── Format: [[\"item_id\", \"turns_into\"], [...], etc]",
                         " • item_id: The item's ID (i.e. \"minecraft:sponge\").",
                         " • turns_into: The item to be turned into when the item is used (i.e. \"minecraft:wet_sponge\").",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Drying Items"), () -> List.of(
                        List.of("minecraft:sponge", "minecraft:wet_sponge")
                ),
                it -> it instanceof List<?> list
                && list.size() == 2
                && list.get(0) instanceof String
                && list.get(1) instanceof String);

        BUILDER.pop();

        /*
         Misc
         */
        BUILDER.push("Misc");

            ITEM_TEMPERATURES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Defines items that affect the player's temperature when in the inventory",
                         " ├── Format: [[\"item_id\", temperature, \"slotRange\", \"trait\", *\"{nbt}\", *maxEffect, *tempLimit], [...], etc]",
                         " └── [* = optional]",
                         " • item_id: The item's ID (i.e. \"minecraft:lava_bucket\").",
                         " • temperature: The temperature change the item will apply to the entity. For core temperature, this is applied every tick",
                         " • slot_range: Either \"inventory\", \"hotbar\", or \"hand\". Defines what slots the item must be in to apply to the entity (inventory includes hotbar)",
                         " • trait: The temperature trait to apply the effect to. Typical values are \"core\" for body temperature or \"world\" for ambient temperature. More on the mod documentation page.",
                         " • *nbt: The NBT data the item must have to apply to the entity.",
                         " • *max_effect: The maximum temperature effect the item can apply to the entity.",
                         " • *tempLimit: The maximum temperature at which this item temp will have any effect.",
                         "   (Based on the given trait. Represents the minimum temp if the item temp is negative)",
                         " ⌄ ")
                .defineListAllowEmpty(List.of("Item Temperatures"), () -> List.of(
                ),
                it -> it instanceof List<?> list && CSMath.betweenInclusive(list.size(), 4, 6)
                        && list.get(0) instanceof String
                        && list.get(1) instanceof Number
                        && list.get(2) instanceof String
                        && list.get(3) instanceof String
                        && (list.size() < 5 || list.get(4) instanceof String)
                        && (list.size() < 6 || list.get(5) instanceof Number));

        BUILDER.pop();

        BUILDER.push("Item Functions");
            FIRE_RESISTANCE_BLOCKS_OVERHEATING = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allow fire resistance to block overheating damage",
                         " ⌄ ")
                .define("Fire Resistance Immunity", defaultDiff.getOrDefault(ConfigSettings.FIRE_RESISTANCE_ENABLED, true));

            ICE_RESISTANCE_BLOCKS_FREEZING = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allow ice resistance to block freezing damage",
                         " ⌄ ")
                .define("Ice Resistance Immunity", defaultDiff.getOrDefault(ConfigSettings.ICE_RESISTANCE_ENABLED, true));

            REQUIRE_THERMOMETER = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Whether a thermometer is required to see exact world and body temperature",
                         " ⌄ ")
                .define("Require Thermometer", defaultDiff.getOrDefault(ConfigSettings.REQUIRE_THERMOMETER, true));
        BUILDER.pop();

        if (CompatManager.isCreateLoaded())
        {
            BUILDER.comment(" Drains pressure from Create's netherite backtank if the player is in a hot/cold environment",
                            " Triggered by the set bonus for wearing a full set of netherite diving gear",
                            " Disabling these settings will not disable the heat/cold protection set bonus",
                            " ⌄ ")
                    .push("Create Backtank");

            HEAT_DRAINS_BACKTANK = BUILDER
                    .define("Heat Drains Backtank", true);
            COLD_DRAINS_BACKTANK = BUILDER
                    .define("Cold Drains Backtank", false);

            BUILDER.pop();
        }
        else
        {   HEAT_DRAINS_BACKTANK = null;
            COLD_DRAINS_BACKTANK = null;
        }

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

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/item.toml");
    }

    public static void save()
    {   SPEC.save();
    }
}