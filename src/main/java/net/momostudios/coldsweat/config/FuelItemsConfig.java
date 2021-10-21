package net.momostudios.coldsweat.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class FuelItemsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> boilerItems;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> iceBoxItems;
    public static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> hearthItems;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> insulatingItems;

    static
    {
        /*
          Boiler Items
         */
        BUILDER.push("BoilerFuelItems");
            boilerItems = BUILDER
                .comment("Defines the items that the Boiler can use as fuel and their values",
                        "Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]")
                .defineList("Boiler", Arrays.asList
                (
                    Arrays.asList("minecraft:coal", "37"),
                    Arrays.asList("minecraft:charcoal", "37"),
                    Arrays.asList("minecraft:coal_block", "333"),
                    Arrays.asList("minecraft:magma_block", "333"),
                    Arrays.asList("minecraft:lava_bucket", "1000")
                ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        /*
          Ice Box Items
         */
        BUILDER.push("IceboxFuelItems");
            iceBoxItems = BUILDER
                .comment("Defines the items that the Ice Box can use as fuel and their values",
                        "Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]")
                .defineList("Icebox", Arrays.asList
                (
                    Arrays.asList("minecraft:snowball", "37"),
                    Arrays.asList("minecraft:clay", "37"),
                    Arrays.asList("minecraft:snow_block", "333"),
                    Arrays.asList("minecraft:water_bucket", "333"),
                    Arrays.asList("minecraft:ice", "333"),
                    Arrays.asList("minecraft:packed_ice", "1000")
                ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        /*
          Hearth Items
         */
        BUILDER.push("HearthFuelItems");
        hearthItems = BUILDER
            .comment("Defines the items that the Hearth can use as fuel and their values",
                    "Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]",
                    "(cold items are a negative number)")
            .defineList("Hearth", Arrays.asList
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
                ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();
        BUILDER.push("InsulatingItems");
        insulatingItems = BUILDER
            .comment("Defines the items that can be used for insulating armor in the Sewing Table",
                    "Format: [[item-id-1], [item-id-2], ...etc]")
            .defineList("Sewing Table", Arrays.asList
                (
                    "minecraft:leather_helmet",
                    "minecraft:leather_chestplate",
                    "minecraft:leather_leggings",
                    "minecraft:leather_boots"
                ), it -> it instanceof String);
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

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/fuel_items.toml");
    }

    public List<? extends List<String>> boilerItems()
    {
        return boilerItems.get();
    }

    public List<? extends List<String>> iceboxItems()
    {
        return iceBoxItems.get();
    }

    public List<? extends List<String>> hearthItems()
    {
        return hearthItems.get();
    }

    public List<? extends String> insulatingItems()
    {
        return insulatingItems.get();
    }
}
