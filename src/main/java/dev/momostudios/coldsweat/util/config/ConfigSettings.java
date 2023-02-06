package dev.momostudios.coldsweat.util.config;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.config.ColdSweatConfig;
import dev.momostudios.coldsweat.config.EntitySettingsConfig;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.config.WorldSettingsConfig;
import dev.momostudios.coldsweat.util.compat.ModGetters;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigSettings
{
    // Settings saved during runtime for easy access
    public static ValueLoader<Map<ResourceLocation, Pair<Double, Double>>> BIOME_TEMPS;
    public static ValueLoader<Map<ResourceLocation, Pair<Double, Double>>> BIOME_OFFSETS;
    public static ValueLoader<Map<ResourceLocation, Double>> DIMENSION_TEMPS;
    public static ValueLoader<Map<ResourceLocation, Double>> DIMENSION_OFFSETS;
    public static ValueLoader<Double[]> SUMMER_TEMPS;
    public static ValueLoader<Double[]> AUTUMN_TEMPS;
    public static ValueLoader<Double[]> WINTER_TEMPS;
    public static ValueLoader<Double[]> SPRING_TEMPS;

    public static ValueLoader<Map<Item, Pair<Double, Double>>> INSULATION_ITEMS;
    public static ValueLoader<Map<Item, Pair<Double, Double>>> INSULATING_ARMORS;

    public static ValueLoader<Map<Item, Double>> VALID_FOODS;

    public static ValueLoader<Integer> WATERSKIN_STRENGTH;

    public static ValueLoader<List<Item>> LAMP_FUEL_ITEMS;

    public static ValueLoader<List<String>> LAMP_DIMENSIONS;

    public static ValueLoader<Map<Item, Double>> BOILER_FUEL;
    public static ValueLoader<Map<Item, Double>> ICEBOX_FUEL;
    public static ValueLoader<Map<Item, Double>> HEARTH_FUEL;

    public static ValueLoader<Triplet<Integer, Integer, Double>> GOAT_FUR_TIMINGS;

    // Makes the settings instantiation collapsible & easier to read
    static
    {
        BIOME_TEMPS = ValueLoader.of(() ->
                ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeTemperatures(), true));

        BIOME_OFFSETS = ValueLoader.of(() ->
                ConfigHelper.getBiomesWithValues(WorldSettingsConfig.getInstance().biomeOffsets(), false));

        DIMENSION_TEMPS = ValueLoader.of(() ->
        {
            Map<ResourceLocation, Double> map = new HashMap<>();
            for (List<?> entry : WorldSettingsConfig.getInstance().dimensionTemperatures())
            {
                map.put(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).doubleValue());
            }
            return map;
        });

        DIMENSION_OFFSETS = ValueLoader.of(() ->
        {
            Map<ResourceLocation, Double> map = new HashMap<>();
            for (List<?> entry : WorldSettingsConfig.getInstance().dimensionOffsets())
            {
                map.put(new ResourceLocation((String) entry.get(0)), ((Number) entry.get(1)).doubleValue());
            }
            return map;
        });

        BOILER_FUEL = ValueLoader.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().boilerItems()));
        HEARTH_FUEL = ValueLoader.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().hearthItems()));
        ICEBOX_FUEL = ValueLoader.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().iceboxItems()));

        INSULATION_ITEMS = ValueLoader.of(() ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().insulatingItems())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {
                    map.put(item, Pair.of(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
                }
            }
            return map;
        });

        INSULATING_ARMORS = ValueLoader.of(() ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (List<?> entry : ItemSettingsConfig.getInstance().insulatingArmor())
            {
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {
                    map.put(item, Pair.of(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
                }
            }
            return map;
        });

        VALID_FOODS = ValueLoader.of(() -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.getInstance().temperatureFoods()));

        WATERSKIN_STRENGTH = ValueLoader.of(() -> ItemSettingsConfig.getInstance().waterskinStrength());

        LAMP_FUEL_ITEMS = ValueLoader.of(() ->
        {
            List<Item> list = new ArrayList<>();
            for (String itemID : ItemSettingsConfig.getInstance().soulLampItems())
            {
                list.addAll(ConfigHelper.getItems(itemID));
            }
            return list;
        });

        LAMP_DIMENSIONS = ValueLoader.of(() -> new ArrayList<>(ItemSettingsConfig.getInstance().soulLampDimensions()));

        GOAT_FUR_TIMINGS = ValueLoader.of(() ->
        {
            List<?> entry = EntitySettingsConfig.getInstance().goatFurGrowth();
            return new Triplet<>(((Number) entry.get(0)).intValue(), ((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).doubleValue());
        });

        if (ModGetters.isSereneSeasonsLoaded())
        {
            SUMMER_TEMPS = ValueLoader.of(() -> WorldSettingsConfig.getInstance().summerTemps());
            AUTUMN_TEMPS = ValueLoader.of(() -> WorldSettingsConfig.getInstance().autumnTemps());
            WINTER_TEMPS = ValueLoader.of(() -> WorldSettingsConfig.getInstance().winterTemps());
            SPRING_TEMPS = ValueLoader.of(() -> WorldSettingsConfig.getInstance().springTemps());
        }
    }

    public int difficulty;
    public double maxTemp;
    public double minTemp;
    public double rate;
    public boolean fireRes;
    public boolean iceRes;
    public boolean damageScaling;
    public boolean requireThermometer;
    public int graceLength;
    public boolean graceEnabled;

    private static ConfigSettings INSTANCE = new ConfigSettings();

    public ConfigSettings() {}

    public static ConfigSettings getInstance()
    {
        return INSTANCE;
    }

    public static void setInstance(ConfigSettings instance)
    {
        INSTANCE = instance;
    }

    public ConfigSettings(ColdSweatConfig config)
    {
        readValues(config);
    }

    public void readValues(ColdSweatConfig config)
    {
        difficulty = config.getDifficulty();
        maxTemp = config.getMaxTempHabitable();
        minTemp = config.getMinTempHabitable();
        rate = config.getRateMultiplier();
        fireRes = config.isFireResistanceEnabled();
        iceRes = config.isIceResistanceEnabled();
        damageScaling = config.doDamageScaling();
        requireThermometer = config.thermometerRequired();
        graceLength = config.getGracePeriodLength();
        graceEnabled = config.isGracePeriodEnabled();
    }
}
