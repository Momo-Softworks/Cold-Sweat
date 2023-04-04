package dev.momostudios.coldsweat.config;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.math.ListBuilder;
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

    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> summerTemps = null;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> autumnTemps = null;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> winterTemps = null;
    public static ForgeConfigSpec.ConfigValue<List<? extends Number>> springTemps = null;

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
            .defineList("Dimension Temperature Offsets", List.of(
                    List.of("minecraft:the_nether", 0.7),
                    List.of("minecraft:the_end", -0.1)
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        dimensionTemps = BUILDER
            .comment("Overrides existing dimension temperatures & offsets",
                     "Also overrides temperatures of all biomes in the dimension")
            .defineList("Dimension Temperatures", List.of(
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
            .defineList("Biome Temperature Offsets", List.of(),
                it ->
                {
                    if (it instanceof List<?> list)
                    {
                        if (list.size() == 2)
                        {
                            ColdSweat.LOGGER.warn("Falling back to legacy code for config setting \"Biome Temperature Offsets\". Please update to the new standard!");
                        }
                        return list.get(0) instanceof String && list.get(1) instanceof Number && (list.size() < 3 || list.get(2) instanceof Number) && (list.size() < 4 || list.get(3) instanceof String);
                    }
                    return false;
                });


        biomeTemps = BUILDER
            .comment("Defines the temperature of a biome, overriding existing biome temperatures & offsets (in Minecraft units).")
            .defineList("Biome Temperatures", ListBuilder.begin(
                            List.of("minecraft:soul_sand_valley", 33, 33, "F"),
                            List.of("minecraft:old_growth_birch_forest", 58, 72, "F"),
                            List.of("minecraft:river", 60, 70, "F"),
                            List.of("minecraft:swamp", 72, 84, "F"),
                            List.of("minecraft:savanna", 70, 95, "F"),
                            List.of("minecraft:savanna_plateau", 76, 98, "F"),
                            List.of("minecraft:windswept_savanna", 67, 90, "F"),
                            List.of("minecraft:taiga", 44, 62, "F"),
                            List.of("minecraft:snowy_taiga", 28, 52, "F"),
                            List.of("minecraft:old_growth_pine_taiga", 48, 62, "F"),
                            List.of("minecraft:old_growth_spruce_taiga", 48, 62, "F"),
                            List.of("minecraft:desert", 57, 115, "F"),
                            List.of("minecraft:stony_shore", 50, 60, "F"),
                            List.of("minecraft:snowy_beach", 38, 52, "F"),
                            List.of("minecraft:snowy_slopes", 24, 38, "F"),
                            List.of("minecraft:windswept_forest", 48, 56, "F"),
                            List.of("minecraft:frozen_peaks", 15, 33, "F"),
                            List.of("minecraft:warm_ocean", 67, 76, "F"),
                            List.of("minecraft:deep_frozen_ocean", 56, 65, "F"),
                            List.of("minecraft:badlands", 84, 120, "F"),
                            List.of("minecraft:wooded_badlands", 80, 108, "F"),
                            List.of("minecraft:eroded_badlands", 88, 120, "F"))
                     .addIf(CompatManager.isBiomesOPlentyLoaded(),
                            () -> List.of("biomesoplenty:bayou", 67, 78, "F"),
                            () -> List.of("biomesoplenty:bog", 62, 73, "F"),
                            () -> List.of("biomesoplenty:fir_clearing", 56, 68, "F"),
                            () -> List.of("biomesoplenty:marsh", 76, 87, "F"),
                            () -> List.of("biomesoplenty:wetland", 63, 74, "F"),
                            () -> List.of("biomesoplenty:field", 64, 85, "F"),
                            () -> List.of("biomesoplenty:ominous_woods", 65, 72, "F"),
                            () -> List.of("biomesoplenty:coniferous_forest", 44, 58, "F"),
                            () -> List.of("biomesoplenty:seasonal_forest", 52, 64, "F"),
                            () -> List.of("biomesoplenty:pumpkin_patch", 57, 78, "F"),
                            () -> List.of("biomesoplenty:woodland", 67, 80, "F"),
                            () -> List.of("biomesoplenty:mediterranean_forest", 64, 78, "F"),
                            () -> List.of("biomesoplenty:dune_beach", 67, 78, "F"),
                            () -> List.of("biomesoplenty:rocky_rainforest", 73, 86, "F"),
                            () -> List.of("biomesoplenty:old_growth_woodland", 65, 78, "F"),
                            () -> List.of("biomesoplenty:forested_field", 64, 78, "F"),
                            () -> List.of("biomesoplenty:fungal_jungle", 73, 86, "F"),
                            () -> List.of("biomesoplenty:highland", 57, 70, "F"),
                            () -> List.of("biomesoplenty:highland_moor", 54, 68, "F"),
                            () -> List.of("biomesoplenty:grassland", 58, 82, "F"),
                            () -> List.of("biomesoplenty:clover_patch", 56, 78, "F"),
                            () -> List.of("biomesoplenty:jade_cliffs", 57, 70, "F"),
                            () -> List.of("biomesoplenty:lush_desert", 72, 94, "F"),
                            () -> List.of("biomesoplenty:dryland", 67, 97, "F"),
                            () -> List.of("biomesoplenty:maple_woods", 58, 68, "F"),
                            () -> List.of("biomesoplenty:mystic_grove", 65, 72, "F"),
                            () -> List.of("biomesoplenty:orchard", 58, 78, "F"),
                            () -> List.of("biomesoplenty:prairie", 66, 82, "F"),
                            () -> List.of("biomesoplenty:origin_valley", 65, 80, "F"),
                            () -> List.of("biomesoplenty:snowy_coniferous_forest", 28, 48, "F"),
                            () -> List.of("biomesoplenty:snowy_fir_clearing", 32, 51, "F"),
                            () -> List.of("biomesoplenty:snowy_maple_woods", 32, 48, "F"),
                            () -> List.of("biomesoplenty:spider_nest", 75, 75, "F"),
                            () -> List.of("biomesoplenty:volcanic_plains", 82, 95, "F"),
                            () -> List.of("biomesoplenty:volcano", 94, 120, "F"),
                            () -> List.of("biomesoplenty:wooded_wasteland", 78, 95, "F"))
                    .addIf(CompatManager.isBiomesYoullGoLoaded(),
                            () -> List.of("byg:coniferous_forest", 52, 70, "F"),
                            () -> List.of("byg:autumnal_forest", 60, 75, "F"),
                            () -> List.of("byg:autumnal_taiga", 56, 68, "F"),
                            () -> List.of("byg:baobab_savanna", 70, 95, "F"),
                            () -> List.of("byg:dacite_ridges", 40, 65, "F"),
                            () -> List.of("byg:firecracker_shrubland", 72, 105, "F"),
                            () -> List.of("byg:frosted_taiga", 22, 48, "F"),
                            () -> List.of("byg:cypress_swamplands", 68, 82, "F"),
                            () -> List.of("byg:dead_sea", 72, 82, "F"),
                            () -> List.of("byg:lush_stacks", 66, 75, "F"),
                            () -> List.of("byg:fragment_forest", 43, 64, "F"),
                            () -> List.of("byg:frosted_coniferous_forest", 8, 31, "F"),
                            () -> List.of("byg:maple_taiga", 53, 71, "F"),
                            () -> List.of("byg:skyris_vale", 65, 78, "F"),
                            () -> List.of("byg:twilight_meadow", 49, 66, "F"),
                            () -> List.of("byg:weeping_witch_forest", 56, 73, "F"),
                            () -> List.of("byg:subzero_hypogeal", -10, -10, "F"),
                            () -> List.of("byg:zelkova_forest", 44, 61, "F"))
                    .addIf(CompatManager.isAtmosphericLoaded(),
                            () -> List.of("atmospheric:dunes", 78, 115, "F"),
                            () -> List.of("atmospheric:flourishing_dunes", 68, 105, "F"),
                            () -> List.of("atmospheric:petrified_dunes", 58, 120, "F"),
                            () -> List.of("atmospheric:rocky_dunes", 55, 125, "F"),
                            () -> List.of("atmospheric:rainforest", 68, 90, "F"),
                            () -> List.of("atmospheric:rainforest_basin", 68, 90, "F"),
                            () -> List.of("atmospheric:sparse_rainforest", 62, 83, "F"),
                            () -> List.of("atmospheric:sparse_rainforest_basin", 62, 83, "F"))
                    .addIf(CompatManager.isEnvironmentalLoaded(),
                            () -> List.of("environmental:marsh", 60, 80, "F"))
                    .addIf(CompatManager.isTerralithLoaded(),
                            () -> List.of("terralith:moonlight_valley", 57, 76),
                            () -> List.of("terralith:rocky_mountains", 52, 73),
                            () -> List.of("terralith:alpine_grove", 16, 53),
                            () -> List.of("terralith:yellowstone", 47, 68),
                            () -> List.of("terralith:forested_highlands", 43, 70),
                            () -> List.of("terralith:temperate_highlands", 54, 80),
                            () -> List.of("terralith:amethyst_rainforest", 69, 84),
                            () -> List.of("terralith:sandstone_valley", 78, 114),
                            () -> List.of("terralith:ancient_sands", 83, 130),
                            () -> List.of("terralith:arid_highlands", 74, 102),
                            () -> List.of("terralith:volcanic_crater", 96, 162),
                            () -> List.of("terralith:volcanic_peaks", 76, 122),
                            () -> List.of("terralith:basalt_cliffs", 76, 122),
                            () -> List.of("terralith:birch_taiga", 40, 62),
                            () -> List.of("terralith:brushland", 64, 89),
                            () -> List.of("terralith:bryce_canyon", 72, 110),
                            () -> List.of("terralith:caldera", 58, 71),
                            () -> List.of("terralith:cloud_forest", 38, 58),
                            () -> List.of("terralith:desert_canyon", 73, 121),
                            () -> List.of("terralith:desert_spires", 60, 121),
                            () -> List.of("terralith:orchid_swamp", 62, 81),
                            () -> List.of("terralith:fractured_savanna", 65, 92),
                            () -> List.of("terralith:savanna_badlands", 68, 99),
                            () -> List.of("terralith:granite_cliffs", 65, 85),
                            () -> List.of("terralith:granite_cliffs", 65, 85),
                            () -> List.of("terralith:haze_mountain", 62, 74),
                            () -> List.of("terralith:highlands", 62, 74),
                            () -> List.of("terralith:lavender_valley", 59, 76),
                            () -> List.of("terralith:lavender_forest", 56, 75),
                            () -> List.of("terralith:red_oasis", 58, 94),
                            () -> List.of("terralith:shield", 48, 68),
                            () -> List.of("terralith:shield_clearing", 28, 80),
                            () -> List.of("terralith:steppe", 44, 78),
                            () -> List.of("terralith:warped_mesa", 66, 84)
                            )
                    .build(),
                it ->
                {
                    if (it instanceof List<?> list)
                    {
                        if (list.size() == 2)
                        {
                            ColdSweat.LOGGER.warn("Falling back to legacy code for config setting \"Biome Temperatures\". Please update to the new standard!");
                        }
                        return list.get(0) instanceof String && list.get(1) instanceof Number && (list.size() < 3 || list.get(2) instanceof Number) && (list.size() < 4 || list.get(3) instanceof String);
                    }
                    return false;
                });

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
    public List<? extends List<?>> biomeOffsets()
    {
        return biomeOffsets.get();
    }
    public List<? extends List<?>> biomeTemperatures()
    {
        return biomeTemps.get();
    }

    public List<? extends List<?>> dimensionOffsets()
    {
        return dimensionOffsets.get();
    }
    public List<? extends List<?>> dimensionTemperatures()
    {
        return dimensionTemps.get();
    }

    public Double[] summerTemps()
    {
        return summerTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] autumnTemps()
    {
        return autumnTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] winterTemps()
    {
        return winterTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }
    public Double[] springTemps()
    {
        return springTemps.get().stream().map(Number::doubleValue).toArray(Double[]::new);
    }

    public void setBiomeOffsets(List<? extends List<?>> list) {
        biomeOffsets.set(list);
    }
    public void setDimensionOffsets(List<? extends List<?>> list) {
        dimensionOffsets.set(list);
    }
    public void setBiomeTemperatures(List<? extends List<?>> list) {
        biomeTemps.set(list);
    }
    public void setDimensionTemperatures(List<? extends List<?>> list) {
        dimensionTemps.set(list);
    }
}
