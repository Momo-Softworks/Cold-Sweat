package net.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> boilerItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> iceBoxItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> hearthItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> soulLampItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> insulatingItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<String>>> insulatingArmor;

    public static final ItemSettingsConfig INSTANCE = new ItemSettingsConfig();

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

        /*
          Soulfire Lamp Items
         */
        BUILDER.push("HellspringLampItems");
        soulLampItems = BUILDER
                .comment("Defines the items that the Ice Box can use as fuel and their values",
                        "Format: [[item-id-1, fuel-amount-1], [item-id-2, fuel-amount-2], ...etc]")
                .defineList("Soulfire Lamp", Arrays.asList
                        (
                                "minecraft:warped_stem",
                                "minecraft:warped_hyphae",
                                "minecraft:stripped_warped_stem",
                                "minecraft:stripped_warped_hyphae",
                                "minecraft:crimson_stem",
                                "minecraft:crimson_hyphae",
                                "minecraft:stripped_crimson_stem",
                                "minecraft:stripped_crimson_hyphae"
                        ), it -> it instanceof String);
        BUILDER.pop();

        /*
         Insulator Items
         */
        BUILDER.push("InsulatorItems");
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

        /*
         Insulating Armor
         */
        BUILDER.push("InsulatingArmor");
        insulatingArmor = BUILDER
            .comment("Defines the items that provide insulation when worn",
                    "Format: [[item-id-1, amount-1], [item-id-2, amount-2], ...etc]")
            .defineList("Insulating Armor Items", Arrays.asList
                    (
                            Arrays.asList("minecraft:leather_helmet", "4"),
                            Arrays.asList("minecraft:leather_chestplate", "7"),
                            Arrays.asList("minecraft:leather_leggings", "5"),
                            Arrays.asList("minecraft:leather_boots", "4")
                    ), it -> ((List) it).get(0) instanceof String && ((List) it).get(1) instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void setup()
    {
        Path configPath = FMLPaths.CONFIGDIR.get();
        Path csConfigPath = Paths.get(configPath.toAbsolutePath().toString(), "coldsweat");

        // Create the config folder
        try {
            Files.createDirectory(csConfigPath);
        }
        catch (Exception e) {}

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/item_settings.toml");
    }

    public static ItemSettingsConfig getInstance()
    {
        return new ItemSettingsConfig();
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

    public List<? extends List<String>> insulatingArmor()
    {
        return insulatingArmor.get();
    }

    public List<? extends String> soulLampItems()
    {
        return soulLampItems.get();
    }
}
