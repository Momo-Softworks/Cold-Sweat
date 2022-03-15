package dev.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WorldTemperatureConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> biomeOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> biomeTemperatures;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> dimensionOffsets;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> dimensionTemperatures;

    public static final WorldTemperatureConfig INSTANCE = new WorldTemperatureConfig();

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
            .defineList("Dimension Temperature Offsets", Arrays.asList(
                    Arrays.asList("minecraft:the_nether", 1.0),
                    Arrays.asList("minecraft:the_end", -0.1)
            ), it -> ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        BUILDER.push("DimensionTemperatures");
        dimensionTemperatures = BUILDER
            .comment("Override their respective offset values",
                "Also override ALL biome temperatures")
            .defineList("Dimension Temperatures", Arrays.asList(
                    // No default values
            ), it -> ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
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
            .defineList("Biome Temperature Offsets", Arrays.asList(
                    Arrays.asList("minecraft:soul_sand_valley", -0.5),

                    Arrays.asList("minecraft:plains", 0.3),

                    Arrays.asList("minecraft:bamboo_jungle", 0.5),
                    Arrays.asList("minecraft:jungle", 0.5),
                    Arrays.asList("minecraft:sparse_jungle", 0.3),

                    Arrays.asList("minecraft:desert", -0.2),

                    Arrays.asList("minecraft:giant_spruce_taiga", 0.2),
                    Arrays.asList("minecraft:giant_spruce_taiga_hills", 0.2),

                    Arrays.asList("minecraft:savanna", 0.2),
                    Arrays.asList("minecraft:savanna_plateau", 0.2),
                    Arrays.asList("minecraft:windswept_savanna", 0.2),

                    Arrays.asList("minecraft:taiga", 0.2),
                    Arrays.asList("minecraft:old_growth_pine_taiga", 0.2),
                    Arrays.asList("minecraft:old_growth_spruce_taiga", 0.2),
                    Arrays.asList("minecraft:snowy_taiga", 0.2),
                    Arrays.asList("minecraft:snowy_slopes", 0.2)
            ), it -> ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        BUILDER.push("BiomeTemperatures");
        biomeTemperatures = BUILDER
            .comment("Temperatures for individual biomes")
            .defineList("Biome Temperatures", Arrays.asList(
                    // No default values
            ), it -> ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
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

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/world_temperatures.toml");
    }

    /*
     * Non-private values for use elsewhere
     */
    public List<? extends List<Object>> biomeOffsets()
    {
        List<List<Object>> list = new ArrayList<>();
        for (List<?> entry : biomeOffsets.get())
        {
            list.add(Arrays.asList(entry.get(0), entry.get(1)));
        }
        return list;
    }
    public List<? extends List<Object>> biomeTemperatures()
    {
        List<List<Object>> list = new ArrayList<>();
        for (List<?> entry : biomeTemperatures.get())
        {
            list.add(List.of(entry.get(0), entry.get(1)));
        }
        return list;
    }

    public List<? extends List<Object>> dimensionOffsets()
    {
        List<List<Object>> list = new ArrayList<>();
        for (List<?> entry : dimensionOffsets.get())
        {
            list.add(Arrays.asList(entry.get(0), entry.get(1)));
        }
        return list;
    }
    public List<? extends List<Object>> dimensionTemperatures()
    {
        List<List<Object>> list = new ArrayList<>();
        for (List<?> entry : dimensionTemperatures.get())
        {
            list.add(Arrays.asList(entry.get(0), entry.get(1)));
        }
        return list;
    }

    public Map<String, List<? extends List<Object>>> getConfigMap()
    {
        Map<String, List<? extends List<Object>>> map = new HashMap<>();
        map.put("biome_temperatures", biomeTemperatures());
        map.put("biome_offsets", biomeOffsets());
        map.put("dimension_temperatures", dimensionTemperatures());
        map.put("dimension_offsets", dimensionOffsets());
        return map;
    }

    public void setBiomeOffsets(List<? extends List<String>> list) {
        biomeOffsets.set(list);
    }
    public void setDimensionOffsets(List<? extends List<String>> list) {
        dimensionOffsets.set(list);
    }
    public void setBiomeTemperatures(List<? extends List<String>> list) {
        biomeTemperatures.set(list);
    }
    public void setDimensionTemperatures(List<? extends List<String>> list) {
        dimensionTemperatures.set(list);
    }
}
