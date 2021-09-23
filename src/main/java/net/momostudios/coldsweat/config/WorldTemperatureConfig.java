package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeConfigSpec;
import net.momostudios.coldsweat.ColdSweat;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldTemperatureConfig
{
    private static final WorldTemperatureConfig INSTANCE;
    private static final ForgeConfigSpec SPEC;
    private static final Path CONFIG_PATH = Paths.get("config/cold-sweat_world_temperatures.toml");

    static
    {
        Pair<WorldTemperatureConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(WorldTemperatureConfig::new);
        INSTANCE = specPair.getLeft();
        SPEC = specPair.getRight();
        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
            .sync()
            .autoreload()
            .writingMode(WritingMode.REPLACE)
            .build();
        config.load();
        config.save();
        SPEC.setConfig(config);
    }

    private final ForgeConfigSpec.ConfigValue<List<List<String>>> biomeOffsets;
    private final ForgeConfigSpec.ConfigValue<List<List<String>>> biomeTemperatures;
    private final ForgeConfigSpec.ConfigValue<List<List<String>>> dimensionOffsets;
    private final ForgeConfigSpec.ConfigValue<List<List<String>>> dimensionTemperatures;

    private WorldTemperatureConfig(ForgeConfigSpec.Builder configSpecBuilder)
    {
        /*
         Dimensions
         */
        configSpecBuilder.comment("Notation: [[\"dimension1\", \"temperature1\"], [\"dimension2\", \"temperature2\"]... etc]",
            "Common dimension IDs: minecraft:overworld, minecraft:the_nether, minecraft:the_end",
            "Note: all temperatures are in Minecraft units")
            .push("Dimensions");

        configSpecBuilder.push("Temperature offsets for dimensions");
        dimensionOffsets = configSpecBuilder
            .define("Dimension Temperature Offsets", Arrays.asList(
            ));
        configSpecBuilder.pop();

        configSpecBuilder.push("Static temperature for dimensions");
        dimensionTemperatures = configSpecBuilder
            .comment("Override their respective offset values",
                "Also override ALL biome temperatures")
            .define("Dimension Temperatures", Arrays.asList(
            ));
        configSpecBuilder.pop();

        configSpecBuilder.pop();

        /*
         Biomes
         */
        configSpecBuilder.comment("Notation: [[\"biome1\", \"temperature1\"], [\"biome2\", \"temperature2\"]... etc]",
            "Note: all temperatures are in Minecraft units")
        .push("Biomes");

        configSpecBuilder.push("Temperature offsets for individual biomes");
        biomeOffsets = configSpecBuilder
            .define("Biome Temperature Offsets", Arrays.asList(
            ));
        configSpecBuilder.pop();

        configSpecBuilder.push("Temperatures for individual biomes");
        biomeTemperatures = configSpecBuilder
            .comment("Override their respective offset values")
            .define("Biome Temperatures", Arrays.asList(
            ));
        configSpecBuilder.pop();
    }

    public static WorldTemperatureConfig getInstance() {
        return INSTANCE;
    }


    /*
     * Non-private values for use elsewhere
     */
    public List<List<String>> biomeOffsets() {
        return biomeOffsets.get();
    }
    public List<List<String>> biomeTemperatures() {
        return biomeTemperatures.get();
    }

    public List<List<String>> dimensionOffsets() {
        return dimensionOffsets.get();
    }
    public List<List<String>> dimensionTemperatures() {
        return dimensionTemperatures.get();
    }

    /*
     * Safe set methods for config values
     */
    public void setBiomeTemperatures(List<List<String>> list) {
        biomeOffsets.set(list);
    }

    public void save() {
        SPEC.save();
    }
}
