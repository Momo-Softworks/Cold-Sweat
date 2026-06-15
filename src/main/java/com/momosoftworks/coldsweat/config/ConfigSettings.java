package com.momosoftworks.coldsweat.config;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.util.DynamicHolder;
import com.momosoftworks.coldsweat.data.codec.Codec;
import com.momosoftworks.coldsweat.data.codec.ConfigCodecs;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Pair;
import com.momosoftworks.coldsweat.util.math.Triplet;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigSettings
{
    public static final Map<String, DynamicHolder<?>> CONFIG_SETTINGS = new HashMap<>();

    public static Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    // Clientside Settings
    public static final DynamicHolder<Boolean> CELSIUS;
    public static final DynamicHolder<Integer> TEMP_OFFSET;
    public static final DynamicHolder<Boolean> SHOW_CONFIG_BUTTON;
    public static final DynamicHolder<Integer[]> CONFIG_BUTTON_OFFSET;
    public static final DynamicHolder<Integer[]> BODY_ICON_OFFSET;
    public static final DynamicHolder<Integer[]> BODY_READOUT_OFFSET;
    public static final DynamicHolder<Integer[]> WORLD_GAUGE_OFFSET;
    public static final DynamicHolder<Boolean> CUSTOM_HOTBAR_LAYOUT;
    public static final DynamicHolder<Boolean> ICON_BOBBING;
    public static final DynamicHolder<Boolean> HEARTH_DEBUG;
    public static final DynamicHolder<Boolean> DISTORTION_EFFECTS;


    // Config Screen Settings
    public static final DynamicHolder<Integer> DIFFICULTY;
    public static final DynamicHolder<Double> MAX_TEMP;
    public static final DynamicHolder<Double> MIN_TEMP;
    public static final DynamicHolder<Double> TEMP_RATE;
    public static final DynamicHolder<Double> TEMP_DAMAGE;
    public static final DynamicHolder<Integer> TEMPERATURE_HURT_INTERVAL;
    public static final DynamicHolder<Double> MODIFIER_TICK_RATE;
    public static final DynamicHolder<Boolean> FIRE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> ICE_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> DAMAGE_SCALING;
    public static final DynamicHolder<Boolean> REQUIRE_THERMOMETER;
    public static final DynamicHolder<Integer> GRACE_LENGTH;
    public static final DynamicHolder<Boolean> GRACE_ENABLED;

    // World Settings
    public static final DynamicHolder<Map<Integer, Triplet<Double, Double, Temperature.Units>>> BIOME_TEMPS;
    public static final DynamicHolder<Map<Integer, Triplet<Double, Double, Temperature.Units>>> BIOME_OFFSETS;
    public static final DynamicHolder<Map<Integer, Pair<Double, Temperature.Units>>> DIMENSION_TEMPS;
    public static final DynamicHolder<Map<Integer, Pair<Double, Temperature.Units>>> DIMENSION_OFFSETS;
    public static final DynamicHolder<Double> CAVE_INSULATION;
    public static final DynamicHolder<Double> SHADE_TEMP_OFFSET;
    public static final DynamicHolder<Double[]> SUMMER_TEMPS;
    public static final DynamicHolder<Double[]> AUTUMN_TEMPS;
    public static final DynamicHolder<Double[]> WINTER_TEMPS;
    public static final DynamicHolder<Double[]> SPRING_TEMPS;

    // Block settings
    public static final DynamicHolder<Integer> BLOCK_RANGE;
    public static final DynamicHolder<Boolean> COLD_SOUL_FIRE;
    public static final DynamicHolder<List<Block>> HEARTH_SPREAD_WHITELIST;
    public static final DynamicHolder<List<Block>> HEARTH_SPREAD_BLACKLIST;
    public static final DynamicHolder<Double> HEARTH_EFFECT;

    // Item settings
    public static final DynamicHolder<Map<Item, Pair<Double, Double>>> INSULATION_ITEMS;
    public static final DynamicHolder<Map<Item, Pair<Double, Double>>> ADAPTIVE_INSULATION_ITEMS;
    public static final DynamicHolder<Map<Item, Pair<Double, Double>>> INSULATING_ARMORS;
    public static final DynamicHolder<Integer[]> INSULATION_SLOTS;
    public static final DynamicHolder<List<ResourceLocation>> INSULATION_BLACKLIST;

    public static final DynamicHolder<Boolean> CHECK_SLEEP_CONDITIONS;

    public static final DynamicHolder<Map<Item, Double>> FOOD_TEMPERATURES;
    public static final DynamicHolder<Map<Item, Double>> ITEM_TEMPERATURES;

    public static final DynamicHolder<Integer> WATERSKIN_STRENGTH;
    public static final DynamicHolder<Double> INSULATION_STRENGTH;
    public static final DynamicHolder<Double> SOULSPRING_LAMP_STRENGTH;

    public static final DynamicHolder<Map<Item, Integer>> LAMP_FUEL_ITEMS;
    public static final DynamicHolder<List<ResourceLocation>> LAMP_DIMENSIONS;

    public static final DynamicHolder<Map<Item, Double>> BOILER_FUEL;
    public static final DynamicHolder<Map<Item, Double>> ICEBOX_FUEL;
    public static final DynamicHolder<Map<Item, Double>> HEARTH_FUEL;
    public static final DynamicHolder<Boolean> HEARTH_POTIONS_ENABLED;
    public static final DynamicHolder<List<ResourceLocation>> BLACKLISTED_POTIONS;

    // Attribute trait toggles
    public static final DynamicHolder<Boolean> COLD_RESISTANCE_ENABLED;
    public static final DynamicHolder<Boolean> HEAT_RESISTANCE_ENABLED;

    // Acclimation
    public static final DynamicHolder<Double> ACCLIMATION_SPEED;
    public static final DynamicHolder<Pair<Double, Double>> MIN_ACCLIMATION_RANGE;
    public static final DynamicHolder<Pair<Double, Double>> MAX_ACCLIMATION_RANGE;

    // Nearby-entity climate temperatures (normalized entity id -> (temperature, range))
    public static final DynamicHolder<Map<String, Pair<Double, Double>>> ENTITY_CLIMATE_TEMPS;

    // Entity Settings
    public static final DynamicHolder<Triplet<Integer, Integer, Double>> LLAMA_FUR_TIMINGS = DynamicHolder.simple("llama_fur_timings", () -> new Triplet<>(24000, 24000, 0.5));
    public static final DynamicHolder<Map<ResourceLocation, Integer>> CHAMELEON_BIOMES = DynamicHolder.simple("chameleon_biomes", () -> new HashMap<>());
    public static final DynamicHolder<Map<ResourceLocation, Integer>> LLAMA_BIOMES = DynamicHolder.simple("llama_biomes", () -> new HashMap<>());

    // Reusable composite codecs for the config value types
    private static final Codec<Triplet<Double, Double, Temperature.Units>> BIOME_VALUE_CODEC = ConfigCodecs.triplet(Codec.DOUBLE, Codec.DOUBLE, ConfigCodecs.UNITS);
    private static final Codec<Map<Integer, Triplet<Double, Double, Temperature.Units>>> BIOME_MAP_CODEC = ConfigCodecs.map(Codec.INT, BIOME_VALUE_CODEC);
    private static final Codec<Map<Integer, Pair<Double, Temperature.Units>>> DIMENSION_MAP_CODEC = ConfigCodecs.map(Codec.INT, ConfigCodecs.pair(Codec.DOUBLE, ConfigCodecs.UNITS));
    private static final Codec<Map<Item, Pair<Double, Double>>> ITEM_DOUBLE_PAIR_MAP_CODEC = ConfigCodecs.map(ConfigCodecs.ITEM, ConfigCodecs.pair(Codec.DOUBLE, Codec.DOUBLE));

    static
    {
        CELSIUS = addSetting("celsius", () -> ClientSettingsConfig.celsius);

        TEMP_OFFSET = addSetting("temp_offset", () -> ClientSettingsConfig.tempOffset);

        SHOW_CONFIG_BUTTON = addSetting("show_config_button", () -> ClientSettingsConfig.showConfigButton);

        CONFIG_BUTTON_OFFSET = addSetting("config_button_offset", () -> Arrays.stream(ConfigHelper.deserializeArray(ClientSettingsConfig.configButtonPos)).map(Integer::valueOf).toArray(Integer[]::new));

        BODY_ICON_OFFSET = addSetting("body_icon_offset", () -> Arrays.stream(ConfigHelper.deserializeArray(ClientSettingsConfig.bodyIconPos)).map(Integer::valueOf).toArray(Integer[]::new));

        BODY_READOUT_OFFSET = addSetting("body_readout_offset", () -> Arrays.stream(ConfigHelper.deserializeArray(ClientSettingsConfig.bodyReadoutPos)).map(Integer::valueOf).toArray(Integer[]::new));

        WORLD_GAUGE_OFFSET = addSetting("world_gauge_offset", () -> Arrays.stream(ConfigHelper.deserializeArray(ClientSettingsConfig.worldGaugePos)).map(Integer::valueOf).toArray(Integer[]::new));

        CUSTOM_HOTBAR_LAYOUT = addSetting("custom_hotbar_layout", () -> ClientSettingsConfig.customHotbarLayout);

        ICON_BOBBING = addSetting("icon_bobbing", () -> ClientSettingsConfig.iconBobbing);

        HEARTH_DEBUG = addSetting("hearth_debug", () -> ClientSettingsConfig.hearthDebug);

        DISTORTION_EFFECTS = addSetting("distortion_effects", () -> ClientSettingsConfig.distortionEffects);

        DIFFICULTY = addSyncedSetting("difficulty", () -> ColdSweatConfig.difficulty, Codec.INT,
                                      saver -> ColdSweatConfig.difficulty = saver);

        MAX_TEMP = addSyncedSetting("max_temp", () -> ColdSweatConfig.maxHabitable, Codec.DOUBLE,
                                    saver -> ColdSweatConfig.maxHabitable = saver);

        MIN_TEMP = addSyncedSetting("min_temp", () -> ColdSweatConfig.minHabitable, Codec.DOUBLE,
                                    saver -> ColdSweatConfig.minHabitable = saver);

        TEMP_RATE = addSyncedSetting("temp_rate", () -> ColdSweatConfig.rateMultiplier, Codec.DOUBLE,
                                     saver -> ColdSweatConfig.rateMultiplier = saver);

        TEMP_DAMAGE = addSyncedSetting("temp_damage", () -> ColdSweatConfig.tempDamage, Codec.DOUBLE,
                                       saver -> ColdSweatConfig.tempDamage = saver);

        TEMPERATURE_HURT_INTERVAL = addSyncedSetting("temperature_hurt_interval", () -> ColdSweatConfig.tempHurtInterval, Codec.INT,
                                                     saver -> ColdSweatConfig.tempHurtInterval = saver);

        MODIFIER_TICK_RATE = addSyncedSetting("modifier_tick_rate", () -> ColdSweatConfig.modifierTickRate, Codec.DOUBLE,
                                              saver -> ColdSweatConfig.modifierTickRate = saver);

        FIRE_RESISTANCE_ENABLED = addSyncedSetting("fire_resistance_enabled", () -> ColdSweatConfig.fireResistanceEffect, Codec.BOOL,
                                                   saver -> ColdSweatConfig.fireResistanceEffect = saver);

        ICE_RESISTANCE_ENABLED = addSyncedSetting("ice_resistance_enabled", () -> ColdSweatConfig.iceResistanceEffect, Codec.BOOL,
                                                  saver -> ColdSweatConfig.iceResistanceEffect = saver);

        DAMAGE_SCALING = addSyncedSetting("damage_scaling", () -> ColdSweatConfig.damageScaling, Codec.BOOL,
                                          saver -> ColdSweatConfig.damageScaling = saver);

        REQUIRE_THERMOMETER = addSyncedSetting("require_thermometer", () -> ColdSweatConfig.requireThermometer, Codec.BOOL,
                                               saver -> ColdSweatConfig.requireThermometer = saver);

        GRACE_LENGTH = addSyncedSetting("grace_length", () -> ColdSweatConfig.gracePeriodLength, Codec.INT,
                                        saver -> ColdSweatConfig.gracePeriodLength = saver);

        GRACE_ENABLED = addSyncedSetting("grace_enabled", () -> ColdSweatConfig.gracePeriodEnabled, Codec.BOOL,
                                         saver -> ColdSweatConfig.gracePeriodEnabled = saver);

        BLOCK_RANGE = addSyncedSetting("block_range", () -> WorldSettingsConfig.blockRange, Codec.INT,
                                       saver -> WorldSettingsConfig.blockRange = saver);

        COLD_SOUL_FIRE = addSetting("cold_soul_fire", () -> ColdSweatConfig.coldSoulFire);

        HEARTH_EFFECT = addSetting("hearth_effect", () -> ColdSweatConfig.hearthEffect);

        HEARTH_SPREAD_WHITELIST = addSyncedSetting("hearth_spread_whitelist", () -> ConfigHelper.getBlocks(ColdSweatConfig.hearthSpreadWhitelist),
        Codec.list(ConfigCodecs.BLOCK),
        saver -> ColdSweatConfig.hearthSpreadWhitelist = ConfigHelper.serializeList(saver.stream().map(ConfigHelper::getBlockID).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())));

        HEARTH_SPREAD_BLACKLIST = addSyncedSetting("hearth_spread_blacklist", () -> ConfigHelper.getBlocks(ColdSweatConfig.hearthSpreadBlacklist),
        Codec.list(ConfigCodecs.BLOCK),
        saver -> ColdSweatConfig.hearthSpreadBlacklist = ConfigHelper.serializeList(saver.stream().map(ConfigHelper::getBlockID).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())));

        CAVE_INSULATION = addSyncedSetting("cave_insulation", () -> WorldSettingsConfig.caveInsulation, Codec.DOUBLE,
        saver -> WorldSettingsConfig.caveInsulation = saver);

        SHADE_TEMP_OFFSET = addSyncedSetting("shade_temp_offset", () -> WorldSettingsConfig.shadeTempOffset, Codec.DOUBLE,
        saver -> WorldSettingsConfig.shadeTempOffset = saver);

        COLD_RESISTANCE_ENABLED = addSyncedSetting("cold_resistance_enabled", () -> ColdSweatConfig.coldResistanceEnabled, Codec.BOOL,
        saver -> ColdSweatConfig.coldResistanceEnabled = saver);

        HEAT_RESISTANCE_ENABLED = addSyncedSetting("heat_resistance_enabled", () -> ColdSweatConfig.heatResistanceEnabled, Codec.BOOL,
        saver -> ColdSweatConfig.heatResistanceEnabled = saver);

        ACCLIMATION_SPEED = addSyncedSetting("acclimation_speed", () -> ColdSweatConfig.acclimationSpeed, Codec.DOUBLE,
        saver -> ColdSweatConfig.acclimationSpeed = saver);

        MIN_ACCLIMATION_RANGE = addSetting("min_acclimation_range", () -> Pair.of(ColdSweatConfig.minAcclimationLower, ColdSweatConfig.minAcclimationUpper));
        MAX_ACCLIMATION_RANGE = addSetting("max_acclimation_range", () -> Pair.of(ColdSweatConfig.maxAcclimationLower, ColdSweatConfig.maxAcclimationUpper));

        ENTITY_CLIMATE_TEMPS = addSetting("entity_climate_temps", () -> ConfigHelper.getEntitiesWithValues(EntitySettingsConfig.entityTemperatures));

        BIOME_TEMPS = addSyncedSetting("biome_temps", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.biomeTemps, true),
        BIOME_MAP_CODEC,
        saver -> WorldSettingsConfig.biomeTemps = ConfigHelper.serializeList(saver.entrySet().stream()
                                        .map(entry ->
                                             {
                                                 Temperature.Units units = entry.getValue().getThird();
                                                 double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                 double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, false);
                                                 return Arrays.asList(entry.getKey().toString(), min, max, units);
                                             })
                                        .collect(Collectors.toList())));

        BIOME_OFFSETS = addSyncedSetting("biome_offsets", () -> ConfigHelper.getBiomesWithValues(WorldSettingsConfig.biomeOffsets, false),
        BIOME_MAP_CODEC,
        saver -> WorldSettingsConfig.biomeOffsets = ConfigHelper.serializeList(saver.entrySet().stream()
                                        .map(entry ->
                                             {
                                                 Temperature.Units units = entry.getValue().getThird();
                                                 double min = Temperature.convertUnits(entry.getValue().getFirst(), Temperature.Units.MC, units, false);
                                                 double max = Temperature.convertUnits(entry.getValue().getSecond(), Temperature.Units.MC, units, false);
                                                 return Arrays.asList(entry.getKey().toString(), min, max, units);
                                             })
                                        .collect(Collectors.toList())));

        DIMENSION_TEMPS = addSyncedSetting("dimension_temps", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.dimensionTemps),
        DIMENSION_MAP_CODEC,
        saver -> WorldSettingsConfig.dimensionTemps = ConfigHelper.serializeList(saver.entrySet().stream()
                                           .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                           .collect(Collectors.toList())));

        DIMENSION_OFFSETS = addSyncedSetting("dimension_offsets", () -> ConfigHelper.getDimensionsWithValues(WorldSettingsConfig.dimensionOffsets),
        DIMENSION_MAP_CODEC,
        saver -> WorldSettingsConfig.dimensionOffsets = ConfigHelper.serializeList(saver.entrySet().stream()
                                           .map(entry -> Arrays.asList(entry.getKey().toString(), entry.getValue().getFirst(), entry.getValue().getSecond().toString()))
                                           .collect(Collectors.toList())));


        boolean ssLoaded = CompatManager.isSereneSeasonsLoaded();
        SUMMER_TEMPS = addSetting("summer_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.summerTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        AUTUMN_TEMPS = addSetting("autumn_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.autumnTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        WINTER_TEMPS = addSetting("winter_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.winterTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});
        SPRING_TEMPS = addSetting("spring_temps", () -> ssLoaded ? Arrays.stream(ConfigHelper.deserializeArray(WorldSettingsConfig.springTemps)).map(Double::valueOf).toArray(Double[]::new) : new Double[]{0d, 0d, 0d});

        INSULATION_ITEMS = addSyncedSetting("insulation_items", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (Object obj : ConfigHelper.deserializeList(ItemSettingsConfig.insulatingItems))
            {
                if (!(obj instanceof List)) continue;
                List<?> entry = (List<?>) obj;
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(Double.parseDouble((String) entry.get(1)), Double.parseDouble((String) entry.get(2))));
                }
            }
            return map;
        },
        ITEM_DOUBLE_PAIR_MAP_CODEC,
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {   ResourceLocation itemID = ConfigHelper.getResourceLocation(GameRegistry.findUniqueIdentifierFor(entry.getKey()));
                list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
            }
            ItemSettingsConfig.insulatingItems = ConfigHelper.serializeList(list);
        });

        ADAPTIVE_INSULATION_ITEMS = addSyncedSetting("adaptive_insulation_items", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (Object obj : ConfigHelper.deserializeList(ItemSettingsConfig.adaptiveInsulatingItems))
            {
                if (!(obj instanceof List)) continue;
                List<?> entry = (List<?>) obj;
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(Double.parseDouble((String) entry.get(1)), Double.parseDouble((String) entry.get(2))));
                }
            }
            return map;
        },
        ITEM_DOUBLE_PAIR_MAP_CODEC,
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {   ResourceLocation itemID = ConfigHelper.getResourceLocation(GameRegistry.findUniqueIdentifierFor(entry.getKey()));
                list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
            }
            ItemSettingsConfig.adaptiveInsulatingItems = ConfigHelper.serializeList(list);
        });

        INSULATING_ARMORS = addSyncedSetting("insulating_armors", () ->
        {
            Map<Item, Pair<Double, Double>> map = new HashMap<>();
            for (Object obj : ConfigHelper.deserializeList(ItemSettingsConfig.insulatingArmor))
            {
                if (!(obj instanceof List)) continue;
                List<?> entry = (List<?>) obj;
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Pair.of(Double.parseDouble((String) entry.get(1)), Double.parseDouble((String) entry.get(2))));
                }
            }
            return map;
        },
        ITEM_DOUBLE_PAIR_MAP_CODEC,
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Pair<Double, Double>> entry : saver.entrySet())
            {   ResourceLocation itemID = ConfigHelper.getResourceLocation(GameRegistry.findUniqueIdentifierFor(entry.getKey()));
                list.add(Arrays.asList(itemID.toString(), entry.getValue().getFirst(), entry.getValue().getSecond()));
            }
            ItemSettingsConfig.insulatingArmor = ConfigHelper.serializeList(list);
        });

        INSULATION_SLOTS = addSyncedSetting("insulation_slots", () -> Arrays.stream(ConfigHelper.deserializeArray(ItemSettingsConfig.insulationSlots)).map(Integer::valueOf).toArray(Integer[]::new),
        ConfigCodecs.INT_ARRAY,
        saver ->
        {   ItemSettingsConfig.insulationSlots = ConfigHelper.serializeArray(new Integer[] {saver[0], saver[1], saver[2], saver[3]});
        });

        INSULATION_BLACKLIST = addSetting("insulation_blacklist", () -> ConfigHelper.deserializeList(ItemSettingsConfig.insulationBlacklist).stream().map(str -> new ResourceLocation((String) str)).collect(Collectors.toList()));

        CHECK_SLEEP_CONDITIONS = addSetting("check_sleep_conditions", () -> ColdSweatConfig.checkSleep);

        FOOD_TEMPERATURES = addSyncedSetting("food_temperatures", () ->
        {
            Map<Item, Double> map = new HashMap<>();
            for (Object obj : ConfigHelper.deserializeList(ItemSettingsConfig.temperatureFoods))
            {
                if (!(obj instanceof List)) continue;
                List<?> entry = (List<?>) obj;
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Double.parseDouble((String) entry.get(1)));
                }
            }
            return map;
        },
        ConfigCodecs.map(ConfigCodecs.ITEM, Codec.DOUBLE),
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Double> entry : saver.entrySet())
            {   ResourceLocation itemID = ConfigHelper.getResourceLocation(GameRegistry.findUniqueIdentifierFor(entry.getKey()));
                list.add(Arrays.asList(itemID.toString(), entry.getValue()));
            }
            ItemSettingsConfig.temperatureFoods = ConfigHelper.serializeList(list);
        });

        ITEM_TEMPERATURES = addSetting("item_temperatures", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.itemTemperatures));

        WATERSKIN_STRENGTH = addSetting("waterskin_strength", () -> ItemSettingsConfig.waterskinStrength);

        INSULATION_STRENGTH = addSyncedSetting("insulation_strength", () -> ItemSettingsConfig.insulationStrength, Codec.DOUBLE,
        saver -> ItemSettingsConfig.insulationStrength = saver);

        SOULSPRING_LAMP_STRENGTH = addSetting("soulspring_lamp_strength", () -> ItemSettingsConfig.soulLampStrength);

        LAMP_FUEL_ITEMS = addSyncedSetting("lamp_fuel_items", () ->
        {
            Map<Item, Integer> map = new HashMap<>();
            for (Object obj : ConfigHelper.deserializeList(ItemSettingsConfig.soulLampItems))
            {
                if (!(obj instanceof List)) continue;
                List<?> entry = (List<?>) obj;
                String itemID = (String) entry.get(0);
                for (Item item : ConfigHelper.getItems(itemID))
                {   map.put(item, Integer.parseInt((String) entry.get(1)));
                }
            }
            return map;
        },
        ConfigCodecs.map(ConfigCodecs.ITEM, Codec.INT),
        saver ->
        {
            List<List<?>> list = new ArrayList<>();
            for (Map.Entry<Item, Integer> entry : saver.entrySet())
            {   ResourceLocation itemID = ConfigHelper.getResourceLocation(GameRegistry.findUniqueIdentifierFor(entry.getKey()));
                list.add(Arrays.asList(itemID.toString(), entry.getValue()));
            }
            ItemSettingsConfig.soulLampItems = ConfigHelper.serializeList(list);
        });

        LAMP_DIMENSIONS = addSetting("lamp_dimensions", () -> ConfigHelper.deserializeList(ItemSettingsConfig.soulLampDimensions).stream().map(str -> new ResourceLocation((String) str)).collect(Collectors.toList()));

        BOILER_FUEL = addSetting("boiler_fuel", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.boilerItems));
        ICEBOX_FUEL = addSetting("icebox_fuel", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.iceboxItems));
        HEARTH_FUEL = addSetting("hearth_fuel", () -> ConfigHelper.getItemsWithValues(ItemSettingsConfig.hearthItems));

        HEARTH_POTIONS_ENABLED = addSetting("hearth_potions_enabled", () -> ItemSettingsConfig.allowPotionsInHearth);
        BLACKLISTED_POTIONS = addSetting("blacklisted_potions", () -> ConfigHelper.deserializeList(ItemSettingsConfig.blacklistedPotions).stream().map(str -> new ResourceLocation((String) str)).collect(Collectors.toList()));
    }

    public enum Difficulty
    {
        SUPER_EASY(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(40, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(120, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 0.5,
                "require_thermometer", () -> false,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> false
        )),

        EASY(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(45, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(110, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 0.75,
                "require_thermometer", () -> false,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> false
        )),

        NORMAL(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(50, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(100, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 1.0,
                "require_thermometer", () -> true,
                "fire_resistance_enabled", () -> true,
                "ice_resistance_enabled", () -> true,
                "damage_scaling", () -> true
        )),

        HARD(CSMath.mapOf(
                "min_temp", () -> Temperature.convertUnits(60, Temperature.Units.F, Temperature.Units.MC, true),
                "max_temp", () -> Temperature.convertUnits(90, Temperature.Units.F, Temperature.Units.MC, true),
                "temp_rate", () -> 1.5,
                "require_thermometer", () -> true,
                "fire_resistance_enabled", () -> false,
                "ice_resistance_enabled", () -> false,
                "damage_scaling", () -> true
        )),

        CUSTOM(CSMath.mapOf());

        private final Map<String, Supplier<?>> settings;
        Difficulty(Map<String, Supplier<?>> settings)
        {   this.settings = settings;
        }

        public <T> T getSetting(String id)
        {   return (T) settings.get(id).get();
        }

        public <T> T getOrDefault(String id, T defaultValue)
        {   return (T) settings.getOrDefault(id, () -> defaultValue).get();
        }

        public void load()
        {   settings.forEach((id, loader) -> CONFIG_SETTINGS.get(id).setUnsafe(loader.get()));
        }
    }

    public static <T> DynamicHolder<T> addSyncedSetting(String id, Supplier<T> supplier, Codec<T> codec, Consumer<T> saver)
    {   DynamicHolder<T> holder = DynamicHolder.synced(id, supplier, codec, saver);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static <T> DynamicHolder<T> addSetting(String id, Supplier<T> supplier)
    {   DynamicHolder<T> holder = DynamicHolder.simple(id, supplier);
        CONFIG_SETTINGS.put(id, holder);
        return holder;
    }

    public static Map<String, NBTTagCompound> encode()
    {
        Map<String, NBTTagCompound> map = new HashMap<>();
        CONFIG_SETTINGS.forEach((key, value) ->
        {   if (value.isSynced())
            {   try
                {   map.put(key, value.encode());
                }
                catch (Exception e)
                {   ColdSweat.LOGGER.error("Failed to encode config setting \"" + key + "\" for sync", e);
                }
            }
        });
        return map;
    }

    public static void decode(String key, NBTTagCompound tag)
    {
        CONFIG_SETTINGS.computeIfPresent(key, (k, value) ->
        {   value.decode(tag);
            return value;
        });
    }

    public static void saveValues()
    {
        CONFIG_SETTINGS.values().forEach(value ->
        {   if (value.isSynced())
            {   value.save();
            }
        });
    }

    public static void load()
    {   CONFIG_SETTINGS.values().forEach(DynamicHolder::load);
    }
}
