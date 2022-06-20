package dev.momostudios.coldsweat.config;

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

    static final WorldSettingsConfig INSTANCE = new WorldSettingsConfig();

    static
    {
        /*
         Dimensions
         */
        BUILDER.comment("Notation: [[\"dimension1\", \"temperature1\"], [\"dimension2\", \"temperature2\"]... etc]",
            "Common dimension IDs: minecraft:overworld, minecraft:the_nether, minecraft:the_end",
            "Note: all temperatures are in Minecraft units",
            "°F to MC = (x - 32) / 42",
            "°C to MC = x / 23.3")
            .push("Dimensions");

        BUILDER.push("DimensionTemperatureOffset");
        dimensionOffsets = BUILDER
            .defineList("Dimension Temperature Offsets", List.of(
                    List.of("minecraft:the_nether", 1.0),
                    List.of("minecraft:the_end", -0.1)
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        BUILDER.push("DimensionTemperatures");
        dimensionTemps = BUILDER
            .comment("Override their respective offset values",
                "Also override ALL biome temperatures")
            .defineList("Dimension Temperatures", Arrays.asList(
                    // No default values
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        BUILDER.pop();

        /*
         Biomes
         */
        BUILDER.comment("Notation: [[\"biome1\", \"temperature1\"], [\"biome2\", \"temperature2\"]... etc]",
            "Note: all temperatures are in Minecraft units")
        .push("Biomes");

        BUILDER.push("BiomeTemperatureOffsets");
        biomeOffsets = BUILDER
            .defineList("Biome Temperature Offsets", List.of(
                    List.of("minecraft:soul_sand_valley", -0.5),

                    List.of("minecraft:plains", 0.3),

                    List.of("minecraft:bamboo_jungle", 0.5),
                    List.of("minecraft:jungle", 0.5),
                    List.of("minecraft:sparse_jungle", 0.3),

                    List.of("minecraft:desert", -0.2),

                    List.of("minecraft:giant_spruce_taiga", 0.2),
                    List.of("minecraft:giant_spruce_taiga_hills", 0.2),

                    List.of("minecraft:savanna", 0.0),
                    List.of("minecraft:savanna_plateau", 0.0),
                    List.of("minecraft:windswept_savanna", 0.0),

                    List.of("minecraft:taiga", 0.2),
                    List.of("minecraft:old_growth_pine_taiga", 0.2),
                    List.of("minecraft:old_growth_spruce_taiga", 0.2),
                    List.of("minecraft:snowy_taiga", 0.2),
                    List.of("minecraft:snowy_slopes", 0.2)
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        BUILDER.push("BiomeTemperatures");
        biomeTemps = BUILDER
            .comment("Temperatures for individual biomes")
            .defineList("Biome Temperatures", Arrays.asList(
                    // No default values
            ), it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "coldsweat/world_temperatures.toml");
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

    public void copyValues(WorldSettingsConfig config)
    {
        setBiomeOffsets(config.biomeOffsets());
        setBiomeTemperatures(config.biomeTemperatures());
        setDimensionOffsets(config.dimensionOffsets());
        setDimensionTemperatures(config.dimensionOffsets());
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
