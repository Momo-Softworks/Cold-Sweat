package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.serialization.ListBuilder;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WorldSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> biomeOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> biomeTemps;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> dimensionOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> dimensionTemps;

    public static final ForgeConfigSpec.ConfigValue<Double> caveInsulation;

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> blockTemps;
    public static final ForgeConfigSpec.IntValue blockRange;

    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> summerTemps;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> autumnTemps;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> winterTemps;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> springTemps;

    public static final WorldSettingsConfig INSTANCE = new WorldSettingsConfig();

    static
    {
        /*
         Dimensions
         */
        BUILDER.comment("Format: [[\"dimension_1\", temperature1], [\"dimension_2\", temperature2]... etc]",
                        "Common dimension IDs: minecraft:overworld, minecraft:the_nether, minecraft:the_end",
                        "Note: all temperatures are in Minecraft units",
                        "°F to MC = (x - 32) / 42",
                        "°C to MC = x / 23.3")
               .push("Dimensions");

        dimensionOffsets = BUILDER
                .comment("Applies an offset to the world's temperature across an entire dimension")
            .defineList("Dimension Temperature Offsets", Arrays.asList(
                    Arrays.asList("minecraft:the_nether", 1.0),
                    Arrays.asList("minecraft:the_end", -0.1)
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        dimensionTemps = BUILDER
            .comment("Overrides existing dimension temperatures & offsets",
                     "Also overrides temperatures of all biomes in the dimension")
            .defineList("Dimension Temperatures", Arrays.asList(
                    // No default values
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        BUILDER.pop();

        /*
         Biomes
         */
        BUILDER.comment("Format: [[\"biome_1\", tempLow, tempHigh, *units], [\"biome_2\", tempLow, tempHigh, *units]... etc]",
                       "temp-low: The temperature of the biome at midnight",
                       "temp-high: The temperature of the biome at noon",
                       "units: Optional. The units of the temperature (\"C\" or \"F\". Defaults to MC units)",
                       "Note: all temperatures are in Minecraft units")
               .push("Biomes");

        biomeOffsets = BUILDER
            .comment("Applies an offset to the temperature of a biome (in Minecraft units).")
            .defineList("Biome Temperature Offsets", Arrays.asList(),
                it ->
                {
                    if (it instanceof List<?>)
                    {   List<?> list = (List<?>) it;
                        if (list.size() == 2)
                        {   ColdSweat.LOGGER.warn("Falling back to legacy code for config setting \"Biome Temperature Offsets\". Please update to the new standard!");
                        }
                        return list.get(0) instanceof String && list.get(1) instanceof Number && (list.size() < 3 || list.get(2) instanceof Number) && (list.size() < 4 || list.get(3) instanceof String);
                    }
                    return false;
                });


        biomeTemps = BUILDER
            .comment("Defines the temperature of a biome, overriding existing biome temperatures & offsets (in Minecraft units).")
            .defineList("Biome Temperatures", ListBuilder.begin(
                            Arrays.asList("minecraft:soul_sand_valley", 53, 53, "F"),
                            Arrays.asList("minecraft:tall_birch_forest", 58, 72, "F"),
                            Arrays.asList("minecraft:tall_birch_hills", 58, 72, "F"),
                            Arrays.asList("minecraft:river", 60, 70, "F"),
                            Arrays.asList("minecraft:swamp", 72, 84, "F"),
                            Arrays.asList("minecraft:savanna", 70, 95, "F"),
                            Arrays.asList("minecraft:savanna_plateau", 76, 98, "F"),
                            Arrays.asList("minecraft:shattered_savanna", 67, 90, "F"),
                            Arrays.asList("minecraft:shattered_savanna_plateau", 67, 90, "F"),
                            Arrays.asList("minecraft:taiga", 44, 62, "F"),
                            Arrays.asList("minecraft:snowy_taiga", 19, 48, "F"),
                            Arrays.asList("minecraft:old_growth_pine_taiga", 48, 62, "F"),
                            Arrays.asList("minecraft:old_growth_spruce_taiga", 48, 62, "F"),
                            Arrays.asList("minecraft:desert", 48, 115, "F"),
                            Arrays.asList("minecraft:stone_shore", 50, 64, "F"),
                            Arrays.asList("minecraft:snowy_beach", 38, 52, "F"),
                            Arrays.asList("minecraft:snowy_slopes", 24, 38, "F"),
                            Arrays.asList("minecraft:windswept_forest", 48, 66, "F"),
                            Arrays.asList("minecraft:frozen_peaks", 15, 33, "F"),
                            Arrays.asList("minecraft:warm_ocean", 67, 76, "F"),
                            Arrays.asList("minecraft:deep_frozen_ocean", 56, 65, "F"),
                            Arrays.asList("minecraft:jungle", 76, 87, "F"),
                            Arrays.asList("minecraft:bamboo_jungle", 76, 87, "F"),
                            Arrays.asList("minecraft:badlands", 84, 120, "F"),
                            Arrays.asList("minecraft:wooded_badlands_plateau", 80, 108, "F"),
                            Arrays.asList("minecraft:eroded_badlands", 88, 120, "F"))
                     .addIf(CompatManager.isBiomesOPlentyLoaded(),
                            () -> Arrays.asList("biomesoplenty:bayou", 67, 78, "F"),
                            () -> Arrays.asList("biomesoplenty:fir_clearing", 56, 68, "F"),
                            () -> Arrays.asList("biomesoplenty:marsh", 76, 87, "F"),
                            () -> Arrays.asList("biomesoplenty:grassland_clover_patch", 56, 78, "F"),
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
                            () -> Arrays.asList("byg:snowy_coniferous_forest", 8, 31, "F"),
                            () -> Arrays.asList("byg:snowy_coniferous_forest_hills", 8, 31, "F"),
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
                            () -> Arrays.asList("atmospheric:rocky_dunes", 55, 125, "F"),
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
                    {   List<?> list = (List<?>) it;
                        if (list.size() == 2)
                        {   ColdSweat.LOGGER.warn("Falling back to legacy code for config setting \"Biome Temperatures\". Please update to the new standard!");
                        }
                        return list.get(0) instanceof String && list.get(1) instanceof Number && (list.size() < 3 || list.get(2) instanceof Number) && (list.size() < 4 || list.get(3) instanceof String);
                    }
                    return false;
                });

        BUILDER.pop();

        BUILDER.push("Block Temperature");
        blockTemps = BUILDER
                .comment("Allows for adding simple BlockTemps without the use of Java mods",
                         "Format (All temperatures are in Minecraft units):",
                         "[[\"block-ids\", <temperature>, <range (max 7)>, <*true/false: falloff>, <*max effect>, *<predicates>], [etc...], [etc...]]",
                         "(* = optional) (1 °MC = 42 °F/ 23.33 °C)",
                         "",
                         "Arguments:",
                         "block-ids: multiple IDs can be used by separating them with commas (i.e: \"minecraft:torch,minecraft:wall_torch\")",
                         "temperature: the temperature of the block, in Minecraft units",
                         "falloff: the block is less effective as distance increases",
                         "max effect: the maximum temperature change this block can cause to a player (even with multiple blocks)",
                         "predicates: the state that the block has to be in for the temperature to be applied (lit=true for a campfire, for example).",
                         "Multiple predicates can be used by separating them with commas (i.e: \"lit=true,waterlogged=false\")")
                .defineList("BlockTemps", Arrays.asList
                                    (
                                            Arrays.asList("minecraft:soul_fire",     -0.476, 7, true, 0.8),
                                            Arrays.asList("minecraft:fire",           0.476, 7, true, 0.8),
                                            Arrays.asList("minecraft:magma_block",      0.5, 3, true, 0.6),
                                            Arrays.asList("minecraft:soul_campfire", -0.476, 3, true, 0.6, "lit=true"),
                                            Arrays.asList("minecraft:ice",            -0.15, 4, true, 0.5),
                                            Arrays.asList("minecraft:packed_ice",     -0.25, 4, true, 1.0),
                                            Arrays.asList("minecraft:blue_ice",       -0.35, 4, true, 1.0)
                                    ),
                            it ->
                            {
                                if (it instanceof List<?>)
                                {   List<?> list = (List<?>) it;
                                    return list.size() >= 3 && list.get(0) instanceof String && list.get(1) instanceof Number && list.get(2) instanceof Number;
                                }
                                return false;
                            });
        blockRange = BUILDER
                .comment("The maximum range of blocks' area of effect",
                         "Note: This will not change anything unless blocks are configured to utilize the expanded range",
                          "This value is capped at 16 for performance reasons")
                .defineInRange("Block Range", 7, 7, 16);
        BUILDER.pop();

        BUILDER.push("Misc");

        caveInsulation = BUILDER
                .comment("The amount of temperature normalization from being deep underground",
                         "0.0 = no insulation, 1.0 = full insulation")
                .defineInRange("Cave Insulation Strength", 1.0, 0.0, 1.0);

        BUILDER.pop();

        /* Serene Seasons config */
        if (CompatManager.isSereneSeasonsLoaded())
        {
            BUILDER.comment("Format: [season-start, season-mid, season-end]",
                            "Applied as an offset to the world's temperature")
                   .push("Season Temperatures");

            summerTemps = BUILDER
                    .defineList("Summer", Arrays.asList(
                            0.4, 0.6, 0.4
                    ), it -> it instanceof Number);

            autumnTemps = BUILDER
                    .defineList("Autumn", Arrays.asList(
                            0.2, 0, -0.2
                    ), it -> it instanceof Number);

            winterTemps = BUILDER
                    .defineList("Winter", Arrays.asList(
                            -0.4, -0.6, -0.4
                    ), it -> it instanceof Number);

            springTemps = BUILDER
                    .defineList("Spring", Arrays.asList(
                            -0.2, 0, 0.2
                    ), it -> it instanceof Number);

            BUILDER.pop();
        }

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try
        {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception e)
        {
            // Do nothing
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/world_settings.toml");
    }

    public static WorldSettingsConfig getInstance()
    {
        return INSTANCE;
    }

    /*
     * Non-private values for use elsewhere
     */
    public List<? extends List<?>> getBiomeTempOffsets()
    {
        return biomeOffsets.get();
    }
    public List<? extends List<?>> getBiomeTemperatures()
    {
        return biomeTemps.get();
    }

    public List<? extends List<?>> getDimensionTempOffsets()
    {   return dimensionOffsets.get();
    }
    public List<? extends List<?>> getDimensionTemperatures()
    {   return dimensionTemps.get();
    }

    public List<? extends List<Object>> getBlockTemps()
    {   return blockTemps.get();
    }
    public int getBlockRange()
    {   return blockRange.get();
    }

    public double getCaveInsulation()
    {   return caveInsulation.get();
    }

    public Double[] getSummerTemps()
    {   return summerTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] getAutumnTemps()
    {   return autumnTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] getWinterTemps()
    {   return winterTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] getSpringTemps()
    {   return springTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }

    public void setBiomeTemperatures(List<? extends List<?>> temps)
    {   biomeTemps.set(temps);
    }
    public void setBiomeTempOffsets(List<? extends List<?>> offsets)
    {   biomeOffsets.set(offsets);
    }

    public void setBlockTemps(List<? extends List<Object>> temps)
    {   blockTemps.set(temps);
    }
    public void setBlockRange(int range)
    {   blockRange.set(range);
    }

    public void setCaveInsulation(double insulation)
    {   caveInsulation.set(insulation);
    }

    public void setDimensionTemperatures(List<? extends List<?>> temps)
    {   dimensionTemps.set(temps);
    }
    public void setDimensionTempOffsets(List<? extends List<?>> offsets)
    {   dimensionOffsets.set(offsets);
    }
}
