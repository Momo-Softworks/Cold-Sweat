package dev.momostudios.coldsweat.config;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Serializable;
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

    static final WorldSettingsConfig INSTANCE = new WorldSettingsConfig();

    static
    {
        /*
         Dimensions
         */
        BUILDER.comment("Format: [[\"dimension-1\", temperature-1], [\"dimension-2\", temperature-2]... etc]",
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
        BUILDER.comment("Format: [[\"biome-1\", temp-low, temp-high, *units], [\"biome-2\", temp-low, temp-high, *units]... etc]",
                       "temp-low: The temperature of the biome at midnight",
                       "temp-high: The temperature of the biome at noon",
                       "units: Optional. The units of the temperature (\"C\" or \"F\". Defaults to MC units)",
                       "Note: all temperatures are in Minecraft units")
               .push("Biomes");

        /* Vanilla Biomes */
        List<List<? extends Serializable>> biomeBuilder = new ArrayList<>(List.of(
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
                List.of("minecraft:eroded_badlands", 88, 120, "F")
        ));

        /* Biomes o' Plenty biomes */
        if (ModList.get().isLoaded("biomesoplenty"))
        {
            biomeBuilder.addAll(List.of(
                List.of("biomesoplenty:bayou", 67, 78, "F"),
                List.of("biomesoplenty:bog", 62, 73, "F"),
                List.of("biomesoplenty:fir_clearing", 56, 68, "F"),
                List.of("biomesoplenty:marsh", 76, 87, "F"),
                List.of("biomesoplenty:wetland", 63, 74, "F"),
                List.of("biomesoplenty:field", 64, 85, "F"),
                List.of("biomesoplenty:ominous_woods", 65, 72, "F"),
                List.of("biomesoplenty:coniferous_forest", 44, 58, "F"),
                List.of("biomesoplenty:seasonal_forest", 52, 64, "F"),
                List.of("biomesoplenty:pumpkin_patch", 57, 78, "F"),
                List.of("biomesoplenty:woodland", 67, 80, "F"),
                List.of("biomesoplenty:mediterranean_forest", 64, 78, "F"),
                List.of("biomesoplenty:dune_beach", 67, 78, "F"),
                List.of("biomesoplenty:rocky_rainforest", 73, 86, "F"),
                List.of("biomesoplenty:old_growth_woodland", 65, 78, "F"),
                List.of("biomesoplenty:forested_field", 64, 78, "F"),
                List.of("biomesoplenty:fungal_jungle", 73, 86, "F"),
                List.of("biomesoplenty:highland", 57, 70, "F"),
                List.of("biomesoplenty:highland_moor", 54, 68, "F"),
                List.of("biomesoplenty:grassland", 58, 82, "F"),
                List.of("biomesoplenty:clover_patch", 56, 78, "F"),
                List.of("biomesoplenty:jade_cliffs", 57, 70, "F"),
                List.of("biomesoplenty:lush_desert", 72, 94, "F"),
                List.of("biomesoplenty:dryland", 67, 97, "F"),
                List.of("biomesoplenty:maple_woods", 58, 68, "F"),
                List.of("biomesoplenty:mystic_grove", 65, 72, "F"),
                List.of("biomesoplenty:orchard", 58, 78, "F"),
                List.of("biomesoplenty:prairie", 66, 82, "F"),
                List.of("biomesoplenty:origin_valley", 65, 80, "F"),
                List.of("biomesoplenty:snowy_coniferous_forest", 28, 48, "F"),
                List.of("biomesoplenty:snowy_fir_clearing", 32, 51, "F"),
                List.of("biomesoplenty:snowy_maple_woods", 32, 48, "F"),
                List.of("biomesoplenty:spider_nest", 75, 75, "F"),
                List.of("biomesoplenty:volcanic_plains", 82, 95, "F"),
                List.of("biomesoplenty:volcano", 94, 120, "F"),
                List.of("biomesoplenty:wooded_wasteland", 78, 95, "F")));
        }


        biomeOffsets = BUILDER
            .comment("Applies an offset to the temperature of a biome (in Minecraft units).")
            .defineList("Biome Temperature Offsets", List.of(),
                it ->
                {
                    if (it instanceof List list)
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
            .defineList("Biome Temperatures", biomeBuilder,
                it ->
                {
                    if (it instanceof List list)
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
        if (ModList.get().isLoaded("sereneseasons"))
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
