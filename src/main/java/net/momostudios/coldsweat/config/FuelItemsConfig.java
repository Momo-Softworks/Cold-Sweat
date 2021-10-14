package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class FuelItemsConfig
{
    private static final FuelItemsConfig INSTANCE;
    private static final ForgeConfigSpec SPEC;
    private static final Path CONFIG_PATH = Paths.get("config/cold-sweat_fuel_items.toml");

    static
    {
        Pair<FuelItemsConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(FuelItemsConfig::new);
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

    public final ForgeConfigSpec.ConfigValue<List<List<String>>> boilerItems;
    public final ForgeConfigSpec.ConfigValue<List<List<String>>> iceBoxItems;
    public final ForgeConfigSpec.ConfigValue<List<List<String>>> hearthItems;
    public final ForgeConfigSpec.ConfigValue<List<String>> insulatingItems;


    private FuelItemsConfig(ForgeConfigSpec.Builder configSpecBuilder)
    {
        /*
          Boiler Items
         */
        configSpecBuilder.push("Defines the items that the Boiler can use as fuel and their values");
            boilerItems = configSpecBuilder
                .comment("Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]")
                .define("Boiler", Arrays.asList
                (
                    Arrays.asList("minecraft:coal", "37"),
                    Arrays.asList("minecraft:charcoal", "37"),
                    Arrays.asList("minecraft:coal_block", "333"),
                    Arrays.asList("minecraft:magma_block", "333"),
                    Arrays.asList("minecraft:lava_bucket", "1000")
                ));
        configSpecBuilder.pop();

        /*
          Ice Box Items
         */
        configSpecBuilder.push("Defines the items that the Ice Box can use as fuel and their values");
            iceBoxItems = configSpecBuilder
                .comment("Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]")
                .define("Ice Box", Arrays.asList
                (
                    Arrays.asList("minecraft:snowball", "37"),
                    Arrays.asList("minecraft:clay", "37"),
                    Arrays.asList("minecraft:snow_block", "333"),
                    Arrays.asList("minecraft:water_bucket", "333"),
                    Arrays.asList("minecraft:ice", "333"),
                    Arrays.asList("minecraft:packed_ice", "1000")
                ));
        configSpecBuilder.pop();

        /*
          Hearth Items
         */
        configSpecBuilder.push("Defines the items that the Hearth can use as fuel and their values");
        hearthItems = configSpecBuilder
            .comment("Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]",
                    "(cold items are a negative number)")
            .define("Hearth", Arrays.asList
                (
                    // Hot
                    Arrays.asList("minecraft:coal", "37"),
                    Arrays.asList("minecraft:charcoal", "37"),
                    Arrays.asList("minecraft:coal_block", "333"),
                    Arrays.asList("minecraft:magma_block", "333"),
                    Arrays.asList("minecraft:lava_bucket", "1000"),

                    // Cold
                    Arrays.asList("minecraft:snowball", "-37"),
                    Arrays.asList("minecraft:clay", "-37"),
                    Arrays.asList("minecraft:snow_block", "-333"),
                    Arrays.asList("minecraft:water_bucket", "-333"),
                    Arrays.asList("minecraft:ice", "-333"),
                    Arrays.asList("minecraft:packed_ice", "-1000")
                ));
        configSpecBuilder.pop();
        configSpecBuilder.push("Defines the items that can be used for insulating armor in the Sewing Table");
        insulatingItems = configSpecBuilder
            .comment("Format: [[item-id-1], [item-id-2], ...etc]")
            .define("Sewing Table", Arrays.asList
                (
                    "minecraft:leather_helmet",
                    "minecraft:leather_chestplate",
                    "minecraft:leather_leggings",
                    "minecraft:leather_boots"
                ));
        configSpecBuilder.pop();
    }

    public static FuelItemsConfig getInstance()
    {
        return INSTANCE;
    }

    public List<List<String>> boilerItems()
    {
        return boilerItems.get();
    }

    public List<List<String>> iceboxItems()
    {
        return iceBoxItems.get();
    }

    public List<List<String>> hearthItems()
    {
        return hearthItems.get();
    }

    public List<String> insulatingItems()
    {
        return insulatingItems.get();
    }
}
