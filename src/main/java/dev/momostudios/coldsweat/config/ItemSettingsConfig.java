package dev.momostudios.coldsweat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class ItemSettingsConfig
{
    private static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> boilerItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> iceboxItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> hearthItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> soulLampItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> soulLampDimensions;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> insulatingItems;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> insulatingArmor;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> temperatureFoods;
    private static final ForgeConfigSpec.IntValue waterskinStrength;

    static final ItemSettingsConfig INSTANCE = new ItemSettingsConfig();

    static
    {
        /*
          Fuel Items
         */
        BUILDER.push("Fuel Items")
                .comment("Defines items that can use as fuel",
                         "Format: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]");
        boilerItems = BUILDER
                .defineList("Boiler", Arrays.asList
                                (
                                        Arrays.asList("minecraft:coal",         37),
                                        Arrays.asList("minecraft:charcoal",     37),
                                        Arrays.asList("minecraft:coal_block",   333),
                                        Arrays.asList("minecraft:magma_block",  333),
                                        Arrays.asList("minecraft:lava_bucket",  1000)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        iceboxItems = BUILDER
                .defineList("Icebox", Arrays.asList
                                (
                                        Arrays.asList("minecraft:snowball",     37),
                                        Arrays.asList("minecraft:clay",         37),
                                        Arrays.asList("minecraft:snow_block",   333),
                                        Arrays.asList("minecraft:water_bucket", 333),
                                        Arrays.asList("minecraft:ice",          333),
                                        Arrays.asList("minecraft:packed_ice",   1000)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);

        hearthItems = BUILDER
                .comment("Negative values indicate cold fuel")
                .defineList("Hearth", Arrays.asList
                                (
                                        // Hot
                                        Arrays.asList("minecraft:coal",         37),
                                        Arrays.asList("minecraft:charcoal",     37),
                                        Arrays.asList("minecraft:coal_block",   333),
                                        Arrays.asList("minecraft:magma_block",  333),
                                        Arrays.asList("minecraft:lava_bucket",  1000),

                                        // Cold
                                        Arrays.asList("minecraft:snowball",     -37),
                                        Arrays.asList("minecraft:clay",         -37),
                                        Arrays.asList("minecraft:snow_block",   -333),
                                        Arrays.asList("minecraft:water_bucket", -333),
                                        Arrays.asList("minecraft:ice",          -333),
                                        Arrays.asList("minecraft:packed_ice",   -1000)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        /*
          Soulspring Lamp Items
         */
        BUILDER.push("Soulspring Lamp");
        soulLampItems = BUILDER
                .comment("Defines the items that the Soulspring Lamp can use as fuel and their values",
                        "Format: [\"item-id-1\", \"item-id-2\", ...etc]")
                .defineList("Fuel Items", Arrays.asList
                                (
                                        "minecraft:warped_stem",
                                        "minecraft:warped_hyphae",
                                        "minecraft:stripped_warped_stem",
                                        "minecraft:stripped_warped_hyphae",
                                        "minecraft:crimson_stem",
                                        "minecraft:crimson_hyphae",
                                        "minecraft:stripped_crimson_stem",
                                        "minecraft:stripped_crimson_hyphae"
                                ),
                        it -> it instanceof String);

        soulLampDimensions = BUILDER
                .comment("Defines the dimensions that the Soulspring Lamp can be used in",
                        "Format: [\"dimension-id-1\", \"dimension-id-2\", ...etc]")
                .defineList("Valid Dimensions", Arrays.asList
                                (
                                        "minecraft:the_nether"
                                ),
                        it -> it instanceof String);
        BUILDER.pop();

        /*
         Insulator Items
         */
        BUILDER.push("Insulation");
        insulatingItems = BUILDER
                .comment("Defines the items that can be used for insulating armor in the Sewing Table",
                        "Format: [[\"item-id-1\"], [\"item-id-2\"], ...etc]")
                .defineList("Insulation Ingredients", Arrays.asList
                                (
                                        "minecraft:leather_helmet",
                                        "minecraft:leather_chestplate",
                                        "minecraft:leather_leggings",
                                        "minecraft:leather_boots"
                                ),
                        it -> it instanceof String);

        insulatingArmor = BUILDER
                .comment("Defines the items that provide insulation when worn",
                        "Format: [[\"item-id-1\", amount-1], [\"item-id-2\", amount-2], ...etc]")
                .defineList("Insulated Armor", Arrays.asList
                                (
                                        Arrays.asList("minecraft:leather_helmet", 4),
                                        Arrays.asList("minecraft:leather_chestplate", 7),
                                        Arrays.asList("minecraft:leather_leggings", 5),
                                        Arrays.asList("minecraft:leather_boots", 4)
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        BUILDER.pop();

        /*
         Temperature-Affecting Foods
         */
        BUILDER.push("Consumables");
        temperatureFoods = BUILDER
                .comment("Defines items that affect the player's temperature when consumed",
                        "Format: [[item-id-1, amount-1], [item-id-2, amount-2], ...etc]",
                        "Negative values are cold foods, positive values are hot foods")
                .defineList("Temperature-Affecting Foods", Arrays.asList
                                (
                                        // nothing here
                                ),
                        it -> it instanceof List && ((List<?>) it).get(0) instanceof String && ((List<?>) it).get(1) instanceof Number);
        waterskinStrength = BUILDER
                .comment("Defines the amount a player's body temperature will change by when using a waterskin")
                .defineInRange("Waterskin Strength", 50, 0, Integer.MAX_VALUE);
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
        catch (Exception ignored) {}

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, SPEC, "coldsweat/item_settings.toml");
    }

    public void copyValues(ItemSettingsConfig config)
    {
        setBoilerItems(config.boilerItems());
        setIceboxItems(config.iceboxItems());
        setHearthItems(config.hearthItems());
        setInsulatingItems(config.insulatingItems());
        setInsulatingArmor(config.insulatingArmor());
        setSoulLampDimensions(config.soulLampDimensions());
        setSoulLampItems(config.soulLampItems());
        setTemperatureFoods(config.temperatureFoods());
        setWaterskinStrength(config.waterskinStrength());
    }

    public static ItemSettingsConfig getInstance()
    {
        return INSTANCE;
    }

    public List<? extends List<?>> boilerItems()
    {
        return boilerItems.get();
    }

    public List<? extends List<?>> iceboxItems()
    {
        return iceboxItems.get();
    }

    public List<? extends List<?>> hearthItems()
    {
        return hearthItems.get();
    }

    public List<? extends String> insulatingItems()
    {
        return insulatingItems.get();
    }

    public List<? extends List<?>> insulatingArmor()
    {
        return insulatingArmor.get();
    }

    public List<? extends String> soulLampItems()
    {
        return soulLampItems.get();
    }

    public List<? extends List<?>> temperatureFoods()
    {
        return temperatureFoods.get();
    }

    public List<? extends String> soulLampDimensions()
    {
        return soulLampDimensions.get();
    }

    public int waterskinStrength()
    {
        return waterskinStrength.get();
    }

    public void setBoilerItems(List<? extends List<?>> itemMap)
    {
        boilerItems.set(itemMap);
    }

    public void setIceboxItems(List<? extends List<?>> itemMap)
    {
        iceboxItems.set(itemMap);
    }

    public void setHearthItems(List<? extends List<?>> itemMap)
    {
        hearthItems.set(itemMap);
    }

    public void setInsulatingItems(List<? extends String> items)
    {
        insulatingItems.set(items);
    }

    public void setInsulatingArmor(List<? extends List<?>> itemMap)
    {
        insulatingArmor.set(itemMap);
    }

    public void setSoulLampItems(List<? extends String> items)
    {
        soulLampItems.set(items);
    }

    public void setTemperatureFoods(List<? extends List<?>> itemMap)
    {
        temperatureFoods.set(itemMap);
    }

    public void setSoulLampDimensions(List<? extends String> items)
    {
        soulLampDimensions.set(items);
    }

    public void setWaterskinStrength(int strength)
    {
        waterskinStrength.set(strength);
    }
}
