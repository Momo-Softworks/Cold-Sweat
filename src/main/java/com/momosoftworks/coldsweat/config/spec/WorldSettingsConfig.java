package com.momosoftworks.coldsweat.config.spec;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class WorldSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue HOTTEST_TIME;
    public static final ForgeConfigSpec.IntValue COLDEST_TIME;

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> BIOME_TEMP_OFFSETS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> BIOME_TEMPERATURES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DIMENSION_TEMP_OFFSETS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DIMENSION_TEMPERATURES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> STRUCTURE_TEMP_OFFSETS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> STRUCTURE_TEMPERATURES;

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> BLOCK_TEMPERATURES;
    public static final ForgeConfigSpec.IntValue MAX_BLOCK_TEMP_RANGE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> IS_SOUL_FIRE_COLD;

    public static ForgeConfigSpec.ConfigValue<List<?>> SUMMER_TEMPERATURES;
    public static ForgeConfigSpec.ConfigValue<List<?>> AUTUMN_TEMPERATURES;
    public static ForgeConfigSpec.ConfigValue<List<?>> WINTER_TEMPERATURES;
    public static ForgeConfigSpec.ConfigValue<List<?>> SPRING_TEMPERATURES;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SMART_HEARTH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SMART_BOILER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENABLE_SMART_ICEBOX;
    public static final ForgeConfigSpec.ConfigValue<Double> SOURCE_EFFECT_STRENGTH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOURCE_SPREAD_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOURCE_SPREAD_BLACKLIST;

    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_MAX_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_MAX_VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_WARM_UP_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_MAX_INSULATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> HEARTH_FUEL_INTERVAL;

    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_MAX_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_MAX_VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_WARM_UP_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_MAX_INSULATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> BOILER_FUEL_INTERVAL;

    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_MAX_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_MAX_VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_WARM_UP_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_MAX_INSULATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> ICEBOX_FUEL_INTERVAL;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SLEEPING_OVERRIDE_BLOCKS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOULD_CHECK_SLEEP;

    public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_WATER_FREEZE_BEHAVIOR;
    public static final ForgeConfigSpec.ConfigValue<Boolean> CUSTOM_ICE_DROPS;

    public static final ForgeConfigSpec.ConfigValue<List<?>> SHADE_TEMP_OFFSET;

    public static final ForgeConfigSpec.DoubleValue DRYOFF_SPEED;
    public static final ForgeConfigSpec.DoubleValue WATER_SOAK_SPEED;
    public static final ForgeConfigSpec.DoubleValue RAIN_SOAK_SPEED;
    public static final ForgeConfigSpec.ConfigValue<List<?>> DEFAULT_WATER_TEMP;
    public static final ForgeConfigSpec.DoubleValue MAX_RAIN_SOAK;

    static
    {
        BUILDER.comment("─────────────────────────────────────────────────────────────────────────",
                        " Anywhere that uses IDs, such as blocks, biomes, dimensions, and structures, also supports:",
                        " • Tags (e.g. \"#minecraft:is_underground\")",
                        " • Comma-separated lists (e.g. \"minecraft:desert,#minecraft:is_badlands\")",
                        "     Applies the setting to all listed IDs. Can use tags, regular IDs, and negation interchangeably",
                        " • Negation (e.g. \"!minecraft:jungle_leaves\")",
                        "     Useful with lists/tags. Excludes the listed IDs from the setting",
                        "     i.e. \"#minecraft:leaves,!minecraft:jungle_leaves\" (all leaves EXCEPT jungle leaves)",
                        " Settings with \"//v\" will list elements vertically. Removing \"//v\" will list elements in one line",
                        "─────────────────────────────────────────────────────────────────────────");

        /*
         General
         */
        BUILDER.push("General");
            HOTTEST_TIME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " The time of day when the world is at its hottest, in ticks",
                         " └── 0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight")
                .defineInRange("Hottest Time", 6000, 0, 24000);
            COLDEST_TIME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " The time of day when the world is at its coldest, in ticks",
                         " └── 0 = sunrise, 6000 = noon, 12000 = sunset, 18000 = midnight")
                .defineInRange("Coldest Time", 18000, 0, 24000);
        BUILDER.pop();
        /*
         Dimensions
         */
        BUILDER.push("Dimensions");

            DIMENSION_TEMP_OFFSETS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Applies an offset to the world's temperature across an entire dimension",
                         " ├── Format: [[\"dimension_id\", temperature, *units], [...], etc]",
                         " └── [* = optional]",
                         " • dimension_id: The ID of the dimension (e.g. \"minecraft:the_nether\")",
                         " • temperature: The temperature offset to apply to the dimension",
                         " • *units: The units of the temperature (\"f\", \"c\", or \"mc\"). Defaults to Minecraft units (mc))")
            .defineListAllowEmpty(Arrays.asList("Dimension Temperature Offsets"), () -> ListBuilder.begin(
                    Arrays.asList("minecraft:the_nether", 32, "f"),
                    Arrays.asList("minecraft:the_end", -5, "f"))
                .addIf(CompatManager.isTwilightForestLoaded(), () -> Arrays.asList("twilightforest:twilight_forest_type", 10, "f"))
                .addIf(CompatManager.isAetherLoaded(), () -> Arrays.asList("aether:the_aether", 32, "f"))
                .build(),
            it ->
            {
                if (!(it instanceof List<?>)) return false;
                List<?> list = (List<?>) it;
                return list.get(0) instanceof String
                    && list.get(1) instanceof Number
                    && (list.size() < 3 || list.get(2) instanceof String);
            });

            DIMENSION_TEMPERATURES = BUILDER
            .comment("─────────────────────────────────────────────────────────────────────────//v",
                     " Defines the temperature of a dimension, overriding biome and elevation temperature",
                     " ├── Format: [[\"dimension_id\", temperature, *units], [...], etc]",
                     " └── [* = optional]",
                     " • dimension_id: The ID of the dimension (e.g. \"minecraft:the_nether\")",
                     " • temperature: The temperature of the dimension",
                     " • *units: The units of the temperature (\"f\", \"c\", or \"mc\"). Defaults to Minecraft units (mc))")
            .defineListAllowEmpty(Arrays.asList("Dimension Temperatures"), () -> Arrays.asList(
                    // No default values
            ),
            it ->
            {
                if (!(it instanceof List<?>)) return false;
                List<?> list = (List<?>) it;
                return list.get(0) instanceof String
                    && list.get(1) instanceof Number
                    && (list.size() < 3 || ((List<?>) it).get(2) instanceof String);
            });

        BUILDER.pop();

        /*
         Biomes
         */
        BUILDER.push("Biomes");

        BIOME_TEMP_OFFSETS = BUILDER
            .comment("─────────────────────────────────────────────────────────────────────────//v",
                     " Applies an offset to the temperature of a biome",
                     " ├── Format: [[\"biome_id\", lowTemp, highTemp, *units, *waterTemp], [...], etc]",
                     " └── [* = optional]",
                     " • biome_id: The ID of the biome (e.g. \"minecraft:desert\")",
                     " • lowTemp: The temperature offset at midnight",
                     " • highTemp: The temperature offset at noon",
                     " • *units: The units of the temperature (\"f\", \"c\", or \"mc\"). Defaults to Minecraft units (mc))",
                     " • *waterTemp: Offsets the temperature of water in the biome")
            .defineListAllowEmpty(Arrays.asList("Biome Temperature Offsets"), () -> Arrays.asList(),
                it ->
                {
                    if (!(it instanceof List<?>)) return false;
                    List<?> list = (List<?>) it;
                    return list.get(0) instanceof String
                        && list.get(1) instanceof Number
                        && (list.size() < 3 || list.get(2) instanceof Number)
                        && (list.size() < 4 || list.get(3) instanceof String);
                });

        BIOME_TEMPERATURES = BUILDER
            .comment("─────────────────────────────────────────────────────────────────────────//v",
                     " Defines the temperature of a biome, overriding the biome's default temperature",
                     " ├── Format: [[\"biome_id\", lowTemp, highTemp, *units, *waterTemp], [...], etc]",
                     " └── [* = optional]",
                     " • biome_id: The ID of the biome (e.g. \"minecraft:desert\")",
                     " • lowTemp: The temperature of the biome at midnight",
                     " • highTemp: The temperature of the biome at noon",
                     " • *units: The units of the temperature (\"f\", \"c\", or \"mc\"). Defaults to Minecraft units (mc))",
                     " • *waterTemp: The temperature of water in the biome")
            .defineListAllowEmpty(Arrays.asList("Biome Temperatures"), () -> ListBuilder.begin(
                            Arrays.asList("minecraft:badlands", 84, 120, "F", -5),
                            Arrays.asList("minecraft:badlands_plateau", 84, 120, "F", -5),
                            Arrays.asList("minecraft:bamboo_jungle", 76, 87, "F"),
                            Arrays.asList("minecraft:bamboo_jungle_hills", 76, 87, "F"),
                            Arrays.asList("minecraft:beach", 52, 84, "F"),
                            Arrays.asList("minecraft:birch_forest", 47, 77, "F"),
                            Arrays.asList("minecraft:birch_forest_hills", 47, 77, "F"),
                            Arrays.asList("minecraft:cold_ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:dark_forest", 53, 80, "F"),
                            Arrays.asList("minecraft:dark_forest_hills", 53, 80, "F"),
                            Arrays.asList("minecraft:deep_cold_ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:deep_frozen_ocean", 8, 31, "F"),
                            Arrays.asList("minecraft:deep_lukewarm_ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:deep_ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:desert", 48, 115, "F"),
                            Arrays.asList("minecraft:desert_hills", 48, 115, "F"),
                            Arrays.asList("minecraft:desert_lakes", 48, 115, "F"),
                            Arrays.asList("minecraft:eroded_badlands", 88, 120, "F", -5),
                            Arrays.asList("minecraft:flower_forest", 51, 76, "F"),
                            Arrays.asList("minecraft:forest", 51, 76, "F"),
                            Arrays.asList("minecraft:frozen_ocean", 15, 31, "F"),
                            Arrays.asList("minecraft:frozen_river", 15, 31, "F"),
                            Arrays.asList("minecraft:giant_spruce_taiga", 48, 62, "F"),
                            Arrays.asList("minecraft:giant_spruce_taiga_hills", 48, 62, "F"),
                            Arrays.asList("minecraft:giant_tree_taiga", 48, 62, "F"),
                            Arrays.asList("minecraft:giant_tree_taiga_hills", 48, 62, "F"),
                            Arrays.asList("minecraft:gravelly_mountains", 28, 54, "F"),
                            Arrays.asList("minecraft:ice_spikes", 17, 47, "F"),
                            Arrays.asList("minecraft:jungle", 76, 89, "F"),
                            Arrays.asList("minecraft:jungle_edge", 76, 89, "F"),
                            Arrays.asList("minecraft:jungle_hills", 76, 89, "F"),
                            Arrays.asList("minecraft:lukewarm_ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:modified_badlands_plateau", 84, 120, "F", -5),
                            Arrays.asList("minecraft:modified_gravelly_mountains", 28, 54, "F"),
                            Arrays.asList("minecraft:modified_jungle", 76, 89, "F"),
                            Arrays.asList("minecraft:modified_jungle_edge", 76, 89, "F"),
                            Arrays.asList("minecraft:modified_wooded_badlands_plateau", 84, 120, "F", -5),
                            Arrays.asList("minecraft:mountain_edge", 28, 54, "F"),
                            Arrays.asList("minecraft:mountains", 28, 54, "F"),
                            Arrays.asList("minecraft:mushroom_field_shore", 61, 84, "F"),
                            Arrays.asList("minecraft:mushroom_fields", 61, 84, "F"),
                            Arrays.asList("minecraft:ocean", 39, 70, "F"),
                            Arrays.asList("minecraft:plains", 52, 84, "F"),
                            Arrays.asList("minecraft:river", "disable"),
                            Arrays.asList("minecraft:savanna", 70, 95, "F"),
                            Arrays.asList("minecraft:savanna_plateau", 76, 98, "F"),
                            Arrays.asList("minecraft:shattered_savanna", 67, 90, "F"),
                            Arrays.asList("minecraft:shattered_savanna_plateau", 67, 90, "F"),
                            Arrays.asList("minecraft:snowy_beach", 8, 30, "F"),
                            Arrays.asList("minecraft:snowy_mountains", 8, 30, "F"),
                            Arrays.asList("minecraft:snowy_taiga", 8, 30, "F"),
                            Arrays.asList("minecraft:snowy_taiga_hills", 8, 30, "F"),
                            Arrays.asList("minecraft:snowy_taiga_mountains", 8, 30, "F"),
                            Arrays.asList("minecraft:snowy_tundra", 8, 30, "F"),
                            Arrays.asList("minecraft:soul_sand_valley", 53, 53, "F"),
                            Arrays.asList("minecraft:sparse_jungle", 62, 87, "F"),
                            Arrays.asList("minecraft:stone_shore", 50, 64, "F"),
                            Arrays.asList("minecraft:sunflower_plains", 52, 84, "F"),
                            Arrays.asList("minecraft:swamp", 72, 84, "F", 5),
                            Arrays.asList("minecraft:swamp_hills", 72, 84, "F", 5),
                            Arrays.asList("minecraft:taiga", 44, 62, "F"),
                            Arrays.asList("minecraft:taiga_hills", 44, 62, "F"),
                            Arrays.asList("minecraft:taiga_mountains", 44, 62, "F"),
                            Arrays.asList("minecraft:tall_birch_forest", 58, 72, "F"),
                            Arrays.asList("minecraft:tall_birch_hills", 58, 72, "F"),
                            Arrays.asList("minecraft:warm_ocean", 67, 76, "F", 10),
                            Arrays.asList("minecraft:wooded_badlands_plateau", 80, 108, "F", -5),
                            Arrays.asList("minecraft:wooded_hills", 51, 76, "F"),
                            Arrays.asList("minecraft:wooded_mountains", 51, 76, "F"))
                     .addIf(CompatManager.isBiomesOPlentyLoaded(),
                            () -> Arrays.asList("biomesoplenty:bayou", 67, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:fir_clearing", 56, 68, "F"),
                            () -> Arrays.asList("biomesoplenty:marsh", 76, 87, "F", 5),
                            () -> Arrays.asList("biomesoplenty:grassland_clover_patch", 56, 78, "F", -8),
                            () -> Arrays.asList("biomesoplenty:grassland", 56, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:wetland", 63, 74, "F"),
                            () -> Arrays.asList("biomesoplenty:ominous_woods", 65, 72, "F"),
                            () -> Arrays.asList("biomesoplenty:coniferous_forest", 44, 58, "F"),
                            () -> Arrays.asList("biomesoplenty:seasonal_forest", 52, 64, "F"),
                            () -> Arrays.asList("biomesoplenty:woodland", 67, 80, "F"),
                            () -> Arrays.asList("biomesoplenty:mediterranean_forest", 64, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:dune_beach", 67, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:rainforest_cliffs", 73, 86, "F"),
                            () -> Arrays.asList("biomesoplenty:fungal_jungle", 73, 86, "F"),
                            () -> Arrays.asList("biomesoplenty:highland", 57, 70, "F"),
                            () -> Arrays.asList("biomesoplenty:highland_moor", 54, 68, "F"),
                            () -> Arrays.asList("biomesoplenty:grassland", 58, 82, "F"),
                            () -> Arrays.asList("biomesoplenty:meadow", 56, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:meadow_forest", 56, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:jade_cliffs", 57, 70, "F"),
                            () -> Arrays.asList("biomesoplenty:lush_desert", 72, 94, "F"),
                            () -> Arrays.asList("biomesoplenty:dryland", 67, 97, "F"),
                            () -> Arrays.asList("biomesoplenty:mystic_grove", 65, 72, "F"),
                            () -> Arrays.asList("biomesoplenty:orchard", 58, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:prairie", 66, 82, "F"),
                            () -> Arrays.asList("biomesoplenty:origin_valley", 65, 80, "F"),
                            () -> Arrays.asList("biomesoplenty:snowy_coniferous_forest", 28, 48, "F"),
                            () -> Arrays.asList("biomesoplenty:snowy_fir_clearing", 32, 51, "F"),
                            () -> Arrays.asList("biomesoplenty:snowy_maple_forest", 32, 48, "F"),
                            () -> Arrays.asList("biomesoplenty:volcanic_plains", 82, 95, "F"),
                            () -> Arrays.asList("biomesoplenty:volcano", 94, 120, "F"))
                    .addIf(CompatManager.isBiomesYoullGoLoaded(),
                            () -> Arrays.asList("byg:coniferous_forest", 52, 70, "F"),
                            () -> Arrays.asList("byg:autumnal_valley", 58, 67, "F"),
                            () -> Arrays.asList("byg:seasonal_forest", 60, 75, "F"),
                            () -> Arrays.asList("byg:seasonal_taiga", 56, 68, "F"),
                            () -> Arrays.asList("byg:baobab_savanna", 70, 95, "F"),
                            () -> Arrays.asList("byg:dover_mountains", 40, 65, "F"),
                            () -> Arrays.asList("byg:cypress_swamplands", 68, 82, "F"),
                            () -> Arrays.asList("byg:dead_sea", 72, 82, "F"),
                            () -> Arrays.asList("byg:stone_forest", 43, 64, "F"),
                            () -> Arrays.asList("byg:snowy_coniferous_forest", 8, 31, "F", -15),
                            () -> Arrays.asList("byg:snowy_coniferous_forest_hills", 8, 31, "F", -15),
                            () -> Arrays.asList("byg:maple_taiga", 53, 71, "F"),
                            () -> Arrays.asList("byg:skyris_steeps", 42, 68, "F"),
                            () -> Arrays.asList("byg:skyris_peaks", 42, 68, "F"),
                            () -> Arrays.asList("byg:skyris_highlands", 42, 68, "F"),
                            () -> Arrays.asList("byg:skyris_highlands_clearing", 42, 68, "F"),
                            () -> Arrays.asList("byg:weeping_witch_forest", 56, 73, "F"),
                            () -> Arrays.asList("byg:subzero_hypogeal", -10, -10, "F"),
                            () -> Arrays.asList("byg:zelkova_forest", 44, 61, "F"))
                    .addIf(CompatManager.isAtmosphericLoaded(),
                            () -> Arrays.asList("atmospheric:dunes", 78, 115, "F"),
                            () -> Arrays.asList("atmospheric:dunes_hills", 78, 115, "F"),
                            () -> Arrays.asList("atmospheric:flourishing_dunes", 68, 105, "F"),
                            () -> Arrays.asList("atmospheric:petrified_dunes", 58, 120, "F"),
                            () -> Arrays.asList("atmospheric:rocky_dunes", 55, 125, "F", -5),
                            () -> Arrays.asList("atmospheric:rainforest", 68, 90, "F"),
                            () -> Arrays.asList("atmospheric:rainforest_mountains", 68, 90, "F"),
                            () -> Arrays.asList("atmospheric:rainforest_plateau", 68, 90, "F"),
                            () -> Arrays.asList("atmospheric:rainforest_mountains", 68, 90, "F"),
                            () -> Arrays.asList("atmospheric:rainforest_basin", 68, 90, "F"),
                            () -> Arrays.asList("atmospheric:sparse_rainforest_plateau", 62, 83, "F"),
                            () -> Arrays.asList("atmospheric:sparse_rainforest_basin", 62, 83, "F"))
                    .addIf(CompatManager.isEnvironmentalLoaded(),
                            () -> Arrays.asList("environmental:marsh", 60, 80, "F")
                    ).build(),
                it ->
                {
                    if (it instanceof List<?>)
                    {
                        List<?> list = (List<?>) it;
                        return list.get(0) instanceof String
                            && (list.get(1) instanceof String && list.get(1).equals("disable")
                            || (list.get(1) instanceof Number
                                && list.get(2) instanceof Number
                                && (list.size() < 4 || list.get(3) instanceof String)
                                && (list.size() < 5 || list.get(4) instanceof Number)));
                    }
                    return false;
                }
                );

        BUILDER.pop();


        BUILDER.push("Blocks");

            BLOCK_TEMPERATURES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────//v",
                         " Applies temperature-emitting properties to blocks",
                         " ├── Format: [[\"block_id\", temperature, range, *units, *maxEffect, *\"predicates\", *\"{nbt}\", *tempLimit], [...], etc]",
                         " └── [* = optional]",
                         " • block_id: The ID of the block (i.e. \"minecraft:lava\")",
                         " • temperature: The temperature of the block, in Minecraft units",
                         " • range: The radius of the block's temperature effect, in blocks",
                         " • *units: The units of the temperature (\"f\", \"c\", or \"mc\"). Defaults to Minecraft units (mc))",
                         " • *maxEffect: The maximum cumulative temperature change this block can cause to a player (even with multiple blocks)",
                         " • *predicates: The state that the block must have for the temperature to be applied (i.e. \"lit=true\").",
                         "   (Define multiple predicates by separating them with commas [i.e. \"lit=true,waterlogged=false\"])",
                         " • *nbt: The NBT data that the block must have for the temperature to be applied.",
                         " • *tempLimit: The maximum world temperature at which this block temp will have any effect.",
                         "   (Represents the minimum temp if the block temp is negative)")
                .defineListAllowEmpty(Arrays.asList("Block Temperatures"), () -> ListBuilder.begin(
                                            Arrays.asList("cold_sweat:boiler",         15, 7, "f", 36, "lit=true", "", 212),
                                            Arrays.asList("cold_sweat:icebox",        -15, 7, "f", 36, "frosted=true", "", 32),
                                            Arrays.asList("minecraft:lava",            30, 7, "f", 200, "", "", 1000, true),
                                            Arrays.asList("#minecraft:fire",           25, 7, "f", 50, "", "", 400),
                                            Arrays.asList("#minecraft:campfires",      25, 7, "f", 50, "lit=true", " ", 400),
                                            Arrays.asList("minecraft:magma_block",     20, 3, "f", 48),
                                            Arrays.asList("minecraft:lava_cauldron",   30, 7, "f", 200, "", "", 1000, true),
                                            Arrays.asList("minecraft:ice",            -10, 4, "f", 24, "", "", 33),
                                            Arrays.asList("minecraft:packed_ice",     -15, 4, "f", 48, "", "", 16),
                                            Arrays.asList("minecraft:blue_ice",       -20, 4, "f", 64, "", "", 0),
                                            Arrays.asList("#minecraft:ice",            -6, 4, "f", 27, "", "", 33)
                                      ).addIf(CompatManager.isCreateLoaded(),
                                            () -> Arrays.asList("create:blaze_burner", 5,  3, "f", 30, "blaze=smouldering", "", 400),
                                            () -> Arrays.asList("create:blaze_burner", 12, 4, "f", 30, "blaze=fading", "", 400),
                                            () -> Arrays.asList("create:blaze_burner", 20, 5, "f", 45, "blaze=kindled", "", 400),
                                            () -> Arrays.asList("create:blaze_burner", 30, 6, "f", 60, "blaze=seething", "", 400))
                                      .build(),
                                      it -> {
                                          if (!(it instanceof List<?>)) return false;
                                          List<?> list = (List<?>) it;
                                          return list.size() >= 3
                                              && list.get(0) instanceof String
                                              && (list.get(1) instanceof Number || list.get(1) instanceof String)
                                              && list.get(2) instanceof Number
                                              && (list.size() < 4 || list.get(3) instanceof String)
                                              && (list.size() < 5 || list.get(4) instanceof Number)
                                              && (list.size() < 6 || list.get(5) instanceof String)
                                              && (list.size() < 7 || list.get(6) instanceof String)
                                              && (list.size() < 8 || list.get(7) instanceof Number);
                                      });

            MAX_BLOCK_TEMP_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum range of blocks' area of effect",
                         " - Note: This will not change anything unless blocks are configured to utilize the expanded range",
                         " - This value is limited to 16 for performance reasons")
                .defineInRange("Block Range", 7, 1, 16);

            CUSTOM_WATER_FREEZE_BEHAVIOR = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " When set to true, uses Cold Sweat's temperature system to determine water freezing behavior")
                .define("Custom Freezing Behavior", true);

            CUSTOM_ICE_DROPS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " When set to true, modifies ice blocks to be harvestable with a pickaxe and tweaks its mining speed")
                .define("Custom Ice Drops", true);

            DRYOFF_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The speed at which wet players dry off")
                .defineInRange("Dryoff Speed", 0.0015, 0.0, Double.POSITIVE_INFINITY);

            WATER_SOAK_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The speed at which players become soaked when in water")
                .defineInRange("Water Soak Speed", 0.1, 0.0, Double.POSITIVE_INFINITY);

            RAIN_SOAK_SPEED = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The speed at which players become soaked when in rain")
                .defineInRange("Rain Soak Speed", 0.0125, 0.0, Double.POSITIVE_INFINITY);

            DEFAULT_WATER_TEMP = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The default temperature of water",
                         " └── Format: [temperature, *units]")
                .defineList("Default Water Temperature",
                            Arrays.asList(-10, "f"),
                            it -> it instanceof Number || it instanceof String);

            MAX_RAIN_SOAK = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum soak level a player can reach from rain alone (0.0 - 1.0)")
                .defineInRange("Max Rain Soak", 0.2, 0.0, Double.POSITIVE_INFINITY);

        BUILDER.pop();


        BUILDER.push("Structures");

            STRUCTURE_TEMPERATURES = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Overrides the world temperature when the player is within this structure",
                         " ├── Format: [[\"structure_id\", temperature, *units], [\"structure_id\", temperature, *units], [...], etc]",
                         " └── [* = optional]",
                         " • structure_id: The ID of the structure (i.e. \"minecraft:stronghold\")",
                         " • temperature: The temperature of the structure, in Minecraft units",
                         " • *units: The units of the temperature (\"f\" for Fahrenheit, \"c\" for Celsius, \"mc\" for Minecraft units)")
                .defineListAllowEmpty(Arrays.asList("Structure Temperatures"), () -> Arrays.asList(
                        // empty
                ), it -> it instanceof List<?>
                        && ((List<?>) it).get(0) instanceof String
                        && ((List<?>) it).get(1) instanceof Number
                        && (((List<?>) it).size() < 3 || ((List<?>) it).get(2) instanceof String));

            STRUCTURE_TEMP_OFFSETS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Offsets the world temperature when the player is within this structure",
                         " ├── Format: [[\"structure_id\", offset, *units], [\"structure_id\", offset, *units], [...], etc]",
                         " └── [* = optional]",
                         " • structure_id: The ID of the structure (i.e. \"minecraft:stronghold\")",
                         " • offset: The temperature offset of the structure, in Minecraft units",
                         " • *units: The units of the temperature (\"f\" for Fahrenheit, \"c\" for Celsius, \"mc\" for Minecraft units)")
                .defineListAllowEmpty(Arrays.asList("Structure Temperature Offsets"), () -> Arrays.asList(
                        // empty
                ),
                it ->
                {
                    if (!(it instanceof List<?>)) return false;
                    List<?> list = ((List<?>) it);
                    return list.get(0) instanceof String
                        && list.get(1) instanceof Number
                        && list.size() < 3 || list.get(2) instanceof String;
                });

        BUILDER.pop();


        BUILDER.push("Misc");

            SHADE_TEMP_OFFSET = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " A temperature offset applied when in complete shade or when the sky is overcast",
                         " └── Format: [offset, *units]")
                .defineList("Shade Temperature Offset",
                            Arrays.asList(-0.2, "mc"),
                            it -> it instanceof Number || it instanceof String);

            SLEEPING_OVERRIDE_BLOCKS = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " List of blocks that will allow the player to sleep on them, regardless of the \"Prevent Sleep When in Danger\" setting",
                         " Use this list if the player is not getting the temperature effect from sleeping on particular blocks")
                .defineListAllowEmpty(Arrays.asList("Sleep Check Override Blocks"), () -> ListBuilder.<String>begin()
                        .addIf(CompatManager.modLoaded("comforts"),
                                () -> "#comforts:sleeping_bags")
                        .build(),
                it -> it instanceof String);

            SHOULD_CHECK_SLEEP = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " When set to true, players cannot sleep if they are cold or hot enough to die")
                .define("Check Sleeping Conditions", true);

            IS_SOUL_FIRE_COLD = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Converts damage dealt by Soul Fire to cold damage (default: true)",
                         " Does not affect the block's temperature")
                .define("Cold Soul Fire", true);

        BUILDER.pop();


        BUILDER.comment("\"Thermal sources\" are blocks that have a smokestack and give frigidness/warmth, like the hearth, boiler, and iceobx")
               .push("Thermal Sources");

            SOURCE_EFFECT_STRENGTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " How effective thermal sources are at normalizing temperature")
                .defineInRange("Thermal Source Strength", 0.75, 0, 1.0);

            SOURCE_SPREAD_WHITELIST = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " List of additional blocks that thermal sources can spread through",
                         " Use this list if thermal sources aren't spreading through particular blocks that they should")
                .defineListAllowEmpty(Arrays.asList("Thermal Source Spread Whitelist"), () -> ListBuilder.begin(
                                              "minecraft:iron_bars",
                                              "#minecraft:leaves")
                                          .addIf(CompatManager.isCreateLoaded(),
                                              () -> "create:encased_fluid_pipe")
                                          .build(),
                                      o -> o instanceof String);

            SOURCE_SPREAD_BLACKLIST = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " List of additional blocks that thermal sources spread through",
                         " Use this list if thermal sources are spreading through particular blocks that they shouldn't")
                .defineListAllowEmpty(Arrays.asList("Thermal Source Spread Blacklist"), () -> Arrays.asList(
                                            "minecraft:water"
                ), o -> o instanceof String);

        BUILDER.push("Hearth");

            ENABLE_SMART_HEARTH = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allows the hearth to automatically turn on/off based on nearby players' temperature",
                         " If false, it turns on/off by redstone signal instead")
                .define("Automatic Hearth", false);
            HEARTH_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The distance the hearth's air will travel from a source, like the hearth itself or the end of a pipe")
                .defineInRange("Hearth Range", 20, 0, Integer.MAX_VALUE);
            HEARTH_MAX_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum distance that air can be piped away from the hearth")
                .defineInRange("Max Hearth Range", 96, 0, Integer.MAX_VALUE);
            HEARTH_MAX_VOLUME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum volume of the hearth's area of effect")
                .defineInRange("Hearth Volume", 12000, 1, Integer.MAX_VALUE);
            HEARTH_WARM_UP_TIME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The time it takes for the hearth to be fully functional after being placed")
                .defineInRange("Hearth Warm-Up Time", 1200, 0, Integer.MAX_VALUE);
            HEARTH_MAX_INSULATION = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum amount of insulation that the hearth can provide")
                .defineInRange("Hearth Effect Strength", 10, 0, 10);
            HEARTH_FUEL_INTERVAL = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " How often the hearth consumes fuel (in ticks)",
                         " Lower numbers cause fuel to be consumed faster. Setting to 0 disables fuel consumption")
                .defineInRange("Hearth Fuel Consumption Interval", 40, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Boiler");

            ENABLE_SMART_BOILER = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allows the boiler to automatically turn on/off based on nearby players' temperature",
                         " If false, it turns on/off by redstone signal instead")
                .define("Automatic Boiler", false);
            BOILER_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The distance the boiler's air will travel from a source, like the boiler itself or the end of a pipe")
                .defineInRange("Boiler Range", 16, 0, Integer.MAX_VALUE);
            BOILER_MAX_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum distance that air can be piped away from the boiler")
                .defineInRange("Max Boiler Range", 96, 0, Integer.MAX_VALUE);
            BOILER_MAX_VOLUME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum volume of the boiler's area of effect")
                .defineInRange("Boiler Volume", 2000, 1, Integer.MAX_VALUE);
            BOILER_WARM_UP_TIME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The time it takes for the boiler to be fully functional after being placed")
                .defineInRange("Boiler Warm-Up Time", 1200, 0, Integer.MAX_VALUE);
            BOILER_MAX_INSULATION = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum amount of insulation that the boiler can provide")
                .defineInRange("Boiler Warmth Strength", 5, 0, 10);
            BOILER_FUEL_INTERVAL = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " How often the boiler consumes fuel (in ticks)",
                         " Lower numbers cause fuel to be consumed faster. Setting to 0 disables fuel consumption")
                .defineInRange("Boiler Fuel Consumption Interval", 40, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("Icebox");

            ENABLE_SMART_ICEBOX = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " Allows the icebox to automatically turn on/off based on nearby players' temperature",
                         " If false, it turns on/off by redstone signal instead")
                .define("Automatic Icebox", false);
            ICEBOX_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The distance the icebox's air will travel from a source, like the icebox itself or the end of a pipe")
                .defineInRange("Icebox Range", 16, 0, Integer.MAX_VALUE);
            ICEBOX_MAX_RANGE = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum distance that air can be piped away from the icebox")
                .defineInRange("Max Icebox Range", 96, 0, Integer.MAX_VALUE);
            ICEBOX_MAX_VOLUME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum volume of the icebox's area of effect")
                .defineInRange("Icebox Volume", 2000, 1, Integer.MAX_VALUE);
            ICEBOX_WARM_UP_TIME = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The time it takes for the icebox to be fully functional after being placed")
                .defineInRange("Icebox Warm-Up Time", 1200, 0, Integer.MAX_VALUE);
            ICEBOX_MAX_INSULATION = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " The maximum amount of insulation that the icebox can provide")
                .defineInRange("Icebox Chill Strength", 5, 0, 10);
            ICEBOX_FUEL_INTERVAL = BUILDER
                .comment("─────────────────────────────────────────────────────────────────────────",
                         " How often the icebox consumes fuel (in ticks)",
                         " Lower numbers cause fuel to be consumed faster. Setting to 0 disables fuel consumption")
                .defineInRange("Icebox Fuel Consumption Interval", 40, 0, Integer.MAX_VALUE);

        BUILDER.pop();


        /* Seasons config */
        if (!CompatManager.getSeasonsMods().isEmpty())
        {
            BUILDER.push("Seasons");

            BUILDER.comment("─────────────────────────────────────────────────────────────────────────",
                            " Defines the temperature changes caused by each season",
                            " ├── Format: [seasonStart, seasonMid, seasonEnd, *units]",
                            " └── [* = optional]",
                            " • seasonStart/Mid/End: The temperature offset at the start, middle, and end of the season",
                            " • *units: The unit of temperature (C, F, or MC)",
                            " ⌄ ")
                   .push("Season Temperatures");

                SUMMER_TEMPERATURES = BUILDER
                    .defineList("Summer", Arrays.asList(
                            0.4, 0.6, 0.4
                    ), it -> it instanceof Number || it instanceof String);

                AUTUMN_TEMPERATURES = BUILDER
                    .defineList("Autumn", Arrays.asList(
                            0.2, 0, -0.2
                    ), it -> it instanceof Number || it instanceof String);

                WINTER_TEMPERATURES = BUILDER
                    .defineList("Winter", Arrays.asList(
                            -0.4, -0.6, -0.4
                    ), it -> it instanceof Number || it instanceof String);

                SPRING_TEMPERATURES = BUILDER
                    .defineList("Spring", Arrays.asList(
                            -0.2, 0, 0.2
                    ), it -> it instanceof Number || it instanceof String);

            BUILDER.pop();
        }
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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/world.toml");
    }

    public static void save()
    {   SPEC.save();
    }

    public static List<?> getSummerTemps()
    {   return getSeasonalTemperature(SUMMER_TEMPERATURES);
    }
    public static List<?> getAutumnTemps()
    {   return getSeasonalTemperature(AUTUMN_TEMPERATURES);
    }
    public static List<?> getWinterTemps()
    {   return getSeasonalTemperature(WINTER_TEMPERATURES);
    }
    public static List<?> getSpringTemps()
    {   return getSeasonalTemperature(SPRING_TEMPERATURES);
    }
    private static List<?> getSeasonalTemperature(ForgeConfigSpec.ConfigValue<List<?>> setting)
    {
        return setting.get().stream().map(o ->
        {
            if (o instanceof Number)
            {   return ((Number) o).doubleValue();
            }
            else if (o instanceof String)
            {   return Temperature.Units.fromID((String) o);
            }
            throw new IllegalArgumentException(String.format("Invalid argument \"%s\" for seasonal temperature", o));
        }).collect(Collectors.toList());
    }

    public static synchronized void setSourceSpreadWhitelist(List<ResourceLocation> whitelist)
    {   synchronized (SOURCE_SPREAD_WHITELIST)
        {   SOURCE_SPREAD_WHITELIST.set(whitelist.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        }
    }

    public static synchronized void setSourceSpreadBlacklist(List<ResourceLocation> blacklist)
    {   synchronized (SOURCE_SPREAD_BLACKLIST)
        {   SOURCE_SPREAD_BLACKLIST.set(blacklist.stream().map(ResourceLocation::toString).collect(Collectors.toList()));
        }
    }
}
